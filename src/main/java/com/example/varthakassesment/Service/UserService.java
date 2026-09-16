package com.example.varthakassesment.Service;

import com.example.varthakassesment.DTO.Internal.UserDTO;
import com.example.varthakassesment.Enum.ResponseStatus;
import com.example.varthakassesment.Model.User;
import com.example.varthakassesment.Repo.UserRepo;
import com.example.varthakassesment.Response.GeneralResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public GeneralResponse<List<UserDTO>> getAllUsers(UUID customerId) {

        try {

             List<User> users =
                    userRepo.findByCustomer_CustomerID(customerId);

            List<UserDTO> usersDTO = new ArrayList<>();

            for (User user : users) {

                UUID userCustomerId = null;

                if (user.getCustomer() != null) {
                    userCustomerId = user.getCustomer().getCustomerID();
                }

                UUID roleId = null;

                if (user.getRole() != null) {
                    roleId = user.getRole().getRoleId();
                }

                UUID companyId = null;

                if (user.getCompany() != null) {
                    companyId = user.getCompany().getCompanyId();
                }

                UUID statusId = null;

                if (user.getStatus() != null) {
                    statusId = user.getStatus().getStatusId();
                }

                UserDTO dto = new UserDTO(
                        user.getUserId(),
                        userCustomerId,
                        roleId,
                        companyId,
                        statusId,
                        user.getExternalUserId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getExternalCreatedAt(),
                        user.getExternalUpdatedAt(),
                        user.getActive()
                );

                usersDTO.add(dto);
            }

            return new GeneralResponse<>(
                    ResponseStatus.OK,
                    "Users retrieved successfully",
                    usersDTO
            );

        } catch (Exception ex) {

            return new GeneralResponse<>(
                    ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve users",
                    null
            );
        }
    }


    public GeneralResponse<List<UserDTO>> getUsersByCompany(
            UUID customerId,
            String companyName) {

        try {

            // Tenant + company filtering
            List<User> users =
                    userRepo.findByCustomer_CustomerIDAndCompany_CompanyNameContainingIgnoreCase(
                            customerId,
                            companyName
                    );

            List<UserDTO> usersDTO = new ArrayList<>();

            for (User user : users) {

                UUID userCustomerId = null;

                if (user.getCustomer() != null) {
                    userCustomerId = user.getCustomer().getCustomerID();
                }

                UUID roleId = null;

                if (user.getRole() != null) {
                    roleId = user.getRole().getRoleId();
                }

                UUID companyId = null;

                if (user.getCompany() != null) {
                    companyId = user.getCompany().getCompanyId();
                }

                UUID statusId = null;

                if (user.getStatus() != null) {
                    statusId = user.getStatus().getStatusId();
                }

                UserDTO dto = new UserDTO(
                        user.getUserId(),
                        userCustomerId,
                        roleId,
                        companyId,
                        statusId,
                        user.getExternalUserId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getExternalCreatedAt(),
                        user.getExternalUpdatedAt(),
                        user.getActive()
                );

                usersDTO.add(dto);
            }

            return new GeneralResponse<>(
                    ResponseStatus.OK,
                    "Users retrieved successfully",
                    usersDTO
            );

        } catch (Exception ex) {

            return new GeneralResponse<>(
                    ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve users by company",
                    null
            );
        }
    }
}