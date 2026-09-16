package com.example.varthakassesment.Service;


import com.example.varthakassesment.Client.ClientService.CustomerApiService;
import com.example.varthakassesment.DTO.External.ExCompanyDTO;
import com.example.varthakassesment.DTO.External.ExUserDTO;
import com.example.varthakassesment.DTO.Internal.SyncResponseDTO;
import com.example.varthakassesment.Enum.ResponseStatus;
import com.example.varthakassesment.Mapper.UserMapper;
import com.example.varthakassesment.Model.*;
import com.example.varthakassesment.Repo.*;
import com.example.varthakassesment.Response.GeneralResponse;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SynchronizeUser {

    CustomerApiService ApiService;
    UserRepo _userRepo;
    CustomerRepo _customerRepo;
    CompanyRepo _companyRepo;
    StatusRepo _statusRepo;
    RoleRepo _roleRepo;

    UserMapper _userMapper;

    //dependency injection
    public SynchronizeUser(CustomerApiService apiService, UserRepo _userRepo, CustomerRepo _customerRepo, CompanyRepo _companyRepo, StatusRepo _statusRepo, RoleRepo _roleRepo, UserMapper _userMapper) {
        ApiService = apiService;
        this._userRepo = _userRepo;
        this._customerRepo = _customerRepo;
        this._companyRepo = _companyRepo;
        this._statusRepo = _statusRepo;
        this._roleRepo = _roleRepo;
        this._userMapper = _userMapper;
    }

    @Transactional // the service run as 1 single unit
    public GeneralResponse<SyncResponseDTO> syncUsers(UUID customerId) {

        Optional<Customer> cust = this._customerRepo.findById(customerId);

        if (cust.isEmpty()) {
            return new GeneralResponse<>(
                    ResponseStatus.NOT_FOUND,
                    "Customer not found",
                    null
            );
        }

        Customer customer = cust.get();

        // 1. Fetch all users from external API
        List<ExUserDTO> fetchedUsers;

        try {

            fetchedUsers = this.ApiService.fetchAllPages();

        } catch (Exception ex) {

            return new GeneralResponse<>(
                    ResponseStatus.BAD_GATEWAY,
                    "Failed to communicate with customer API",
                    null
            );
        }


        // Store external IDs returned, Later I'll use them   to detect deleted users
        HashSet<String> externalIds = new HashSet<>();

        //    caches for this synchronization run
        Map<String, Company> companyCache = new HashMap<>();
        Map<String, Role> roleCache = new HashMap<>();
        Map<String, Status> statusCache = new HashMap<>();

//to return number of users created(added to our DB) , updated. deactiv.
        int created = 0;
        int updated = 0;


        // 4. Process each external user
        for (ExUserDTO fetchedUser : fetchedUsers) {

            String externalUserId = fetchedUser.getId();

            //  ID for the deletion/deactivation
            externalIds.add(externalUserId);

            //   Resolve related entities
            Company company = resolveCompany(
                    fetchedUser.getCompany(),
                    customer,
                    companyCache
            );



            Role role = resolveRole(
                    fetchedUser.getCompany().getRole(),
                    roleCache
            ); //Role in EXCompanyDTO:

            Status status = resolveStatus(
                    fetchedUser.getStatus(),
                    statusCache
            );

            //   Checkin if this external user already exists
            Optional<User> existingUser =
                    this._userRepo.findByCustomerAndExternalUserId(
                            customer,
                            externalUserId
                    );

            // CREATE operation

            if (existingUser.isEmpty()) {

                User newUser = new User();

                // Mapping external DTO fields into User
                this._userMapper.MapExUserToEntity(
                        fetchedUser,
                        newUser
                );

                // Fields that are resolved
                newUser.setCustomer(customer);
                newUser.setCompany(company);
                newUser.setRole(role);
                newUser.setStatus(status);
                newUser.setActive(1);


                this._userRepo.save(newUser);

                created++;
            }

            // UPDATE
            else {
                User user = existingUser.get();
                boolean wasInactive = user.getActive() != 1;  //user was inactive now appears again: activated: before this sync processes the user
                boolean changed = !Objects.equals(
                        user.getExternalUpdatedAt(),
                        fetchedUser.getUpdatedAt()
                );
                if (changed || wasInactive) {  //if user deleted, must also get updated in our data base

                    this._userMapper.MapExUserToEntity(fetchedUser, user);
                    user.setCustomer(customer);
                    user.setCompany(company);
                    user.setRole(role);
                    user.setStatus(status);
                    user.setActive(1);
                    this._userRepo.save(user);
                    updated++;
                }
            }
        }

        // Deactivating users that no longer exist when checking data in ex api
        int deactivated = deactivateMissingUsers(customer, externalIds);


        //  Success Response:
        SyncResponseDTO syncResponse = new SyncResponseDTO(
                created,
                updated,
                deactivated
        );

        //GENERAL RESPONSE:
        return new GeneralResponse<>(
                ResponseStatus.OK,
                "User synchronization completed successfully",
                syncResponse
        );
    }


    // COMPANY
    private synchronized Company resolveCompany(
            ExCompanyDTO exCompany,
            Customer customer,
            Map<String, Company> companyCache) {

        String companyName = exCompany.getName();

        // First check cache
        Company company = companyCache.get(companyName);

        if (company == null) {

            // Not in cache -> check database
            company = this._companyRepo.findByCustomerAndCompanyNameIgnoreCase(
                    customer,
                    companyName
            );

            // Not in database -> create
            if (company == null) {

                Company newCompany = new Company();

                newCompany.setCompanyName(exCompany.getName());
                newCompany.setCustomer(customer);
                newCompany.setIndustry(exCompany.getIndustry());
                newCompany.setWebsite(exCompany.getWebsite());
                newCompany.setEmployeesNumber(exCompany.getEmployees());

                // I added this block of code handling: race issue: when 2 threads working
                // simultaneously to add new company for example, here's a unique constraint handling it
                // is turns out this was not enough so, I added this try/catch.
                try {

                    company = this._companyRepo.saveAndFlush(newCompany);

                } catch (DataIntegrityViolationException ex) {

                    // Another synchronization request created the company first
                    // retrieving the Comp. that the other concurrent request created
                    company = this._companyRepo.findByCustomerAndCompanyNameIgnoreCase(
                            customer,
                            companyName
                    );

                    if (company == null) {
                        throw ex;
                    }
                }

            } else {

                // Company already exists -> update its information
                company.setIndustry(exCompany.getIndustry());
                company.setWebsite(exCompany.getWebsite());
                company.setEmployeesNumber(exCompany.getEmployees());

                company = this._companyRepo.save(company);
            }

            // Store resolved company in cache
            companyCache.put(companyName, company);
        }

        return company;
    }

     // ROLE

    private synchronized Role resolveRole(
            String roleName,
            Map<String, Role> roleCache) {

        // First check cache
        Role role = roleCache.get(roleName);

        if (role == null) {

            // Not in cache -> check database
            role = this._roleRepo.findByNameIgnoreCase(roleName);

            // Not in database -> create
            if (role == null) {

                Role newRole = new Role();

                newRole.setName(roleName);

                try {

                    role = this._roleRepo.saveAndFlush(newRole);

                } catch (DataIntegrityViolationException ex) {

                    role = this._roleRepo.findByNameIgnoreCase(roleName);

                    if (role == null) {
                        throw ex;
                    }
                }
            }

            // Store resolved entity in cache
            roleCache.put(roleName, role);
        }

        return role;
    }


    // =========================================================
    // STATUS
    // =========================================================

    private synchronized Status resolveStatus(
            String statusName,
            Map<String, Status> statusCache) {

        // First check cache
        Status status = statusCache.get(statusName);

        if (status == null) {

            // Not in cache -> check database
            status = this._statusRepo.findByStatusNameIgnoreCase(statusName);

            // Not in database -> create
            if (status == null) {

                Status newStatus = new Status();

                newStatus.setStatusName(statusName);

                try {

                    status = this._statusRepo.saveAndFlush(newStatus);

                } catch (DataIntegrityViolationException ex) {

                    // same solution:
                    status = this._statusRepo.findByStatusNameIgnoreCase(statusName);

                    if (status == null) {
                        throw ex;
                    }
                }
            }

            // Store resolved entity in cache
            statusCache.put(statusName, status);
        }

        return status;
    }



    // DEACTIVATING MISSING USERS

    private int deactivateMissingUsers(
            Customer customer,
            HashSet<String> externalIds) {

        int deactivated = 0;

        List<User> activeUsers =
                this._userRepo.findByCustomerAndActive(customer, 1);

        for (User user : activeUsers) {

            if (!externalIds.contains(user.getExternalUserId())) {

                user.setActive(0);
                this._userRepo.save(user);

                deactivated++;
            }
        }

        return deactivated;
    }
}