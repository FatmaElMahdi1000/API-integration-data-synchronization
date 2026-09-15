package com.example.varthakassesment.Service;


import com.example.varthakassesment.Client.ClientService.CustomerApiService;
import com.example.varthakassesment.DTO.External.ExUserDTO;
import com.example.varthakassesment.DTO.Internal.SyncResponseDTO;
import com.example.varthakassesment.Enum.ResponseStatus;
import com.example.varthakassesment.Mapper.UserMapper;
import com.example.varthakassesment.Model.*;
import com.example.varthakassesment.Repo.*;
import com.example.varthakassesment.Response.GeneralResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SynchronizeUser {

    CustomerApiService  ApiService;
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

    @Transactional //so the service run as 1 single unit
    public GeneralResponse<SyncResponseDTO> syncUsers(UUID customerId) {

        Customer customer = this._customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        // 1. Fetch all users from external API
            List<ExUserDTO> fetchedUsers;

            try {

                fetchedUsers = this.ApiService.fetchAllPages();

            } catch (Exception ex) {

                ex.printStackTrace();

                return new GeneralResponse<>(
                        ResponseStatus.BAD_GATEWAY,
                        "Failed to communicate with customer API",
                        null
                );
            }

            try
            {


            // 2. Store external IDs returned by this sync
            //    Later used to detect deleted users
            HashSet<String> externalIds = new HashSet<>();

            // 3. Temporary caches for this synchronization run
            Map<String, Company> companyCache = new HashMap<>();
            Map<String, Role> roleCache = new HashMap<>();
            Map<String, Status> statusCache = new HashMap<>();

//to return number of users created(added to our DB) , updated. deactiv.
            int created = 0;
            int updated = 0;


            // 4. Process each external user
            for (ExUserDTO fetchedUser : fetchedUsers) {

                String externalUserId = fetchedUser.getId();

                // Keep the ID for the deletion/deactivation check
                externalIds.add(externalUserId);

                // 5. Resolve related entities
                Company company = resolveCompany(
                        fetchedUser.getCompany().getName(),
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

                // 6. Check if this external user already exists
                Optional<User> existingUser =
                        this._userRepo.findByCustomerAndExternalUserId(
                                customer,
                                externalUserId
                        );

                // =====================================================
                // CREATE
                // =====================================================
                if (existingUser.isEmpty()) {

                    User newUser = new User();

                    // Map external DTO fields into User
                    this._userMapper.MapExUserToEntity(
                            fetchedUser,
                            newUser
                    );

                    // Fields that are resolved by the sync service
                    newUser.setCustomer(customer);
                    newUser.setCompany(company);
                    newUser.setRole(role);
                    newUser.setStatus(status);
                    newUser.setActive(1);


                    this._userRepo.save(newUser);

                    created++;
                }

                // =====================================================
                // UPDATE
                // =====================================================
                else {

                    User user = existingUser.get();

                    // Update fields coming from external API
                    this._userMapper.MapExUserToEntity(
                            fetchedUser,
                            user
                    );

                    // Update relationships / local fields
                    user.setCustomer(customer);
                    user.setCompany(company);
                    user.setRole(role);
                    user.setStatus(status);
                    user.setActive(1);

                    this._userRepo.save(user);

                    updated++;
                }
            }

            // 7. Deactivate users that no longer exist externally
            int deactivated = deactivateMissingUsers(customer, externalIds);


            //Sync Success Response:
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
        catch(Exception ex)
        {
            ex.printStackTrace(); //log for errors

            return new GeneralResponse<>(ResponseStatus.INTERNAL_SERVER_ERROR ,"User synchronization failed"
                   , null);

        }

    }


    // COMPANY
    private Company resolveCompany(
            String companyName,
            Customer customer,
            Map<String, Company> companyCache) {

        // First check cache
        Company company = companyCache.get(companyName);

        if (company == null) {

            // Not in cache -> check database
            company = this._companyRepo.findByCompanyNameIgnoreCase(companyName);

            // Not in database -> create
            if (company == null) {

                Company newCompany = new Company();

                newCompany.setCompanyName(companyName);
                newCompany.setCustomer(customer);

                company = this._companyRepo.save(newCompany);
            }

            // Store resolved entity in cache
            companyCache.put(companyName, company);
        }

        return company;
    }


    // =========================================================
    // ROLE
    // =========================================================

    private Role resolveRole(
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

                role = this._roleRepo.save(newRole);
            }

            // Store resolved entity in cache
            roleCache.put(roleName, role);
        }

        return role;
    }


    // =========================================================
    // STATUS
    // =========================================================

    private Status resolveStatus(
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

                status = this._statusRepo.save(newStatus);
            }

            // Store resolved entity in cache
            statusCache.put(statusName, status);
        }

        return status;
    }


    // =========================================================
    // DEACTIVATE MISSING USERS
    // =========================================================
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




