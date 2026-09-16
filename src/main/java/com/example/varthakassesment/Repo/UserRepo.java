package com.example.varthakassesment.Repo;

import com.example.varthakassesment.Model.Customer;
import com.example.varthakassesment.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {


    public List<User> findByCustomer_CustomerID(UUID customerId);

    Optional<User> findByCustomerAndExternalUserId(
            Customer customer,
            String externalUserId
    );

    public List<User> findByCustomer_CustomerIDAndCompany_CompanyNameContainingIgnoreCase(UUID customerId, String companyName);
    //With even part of the name of the company will retrieve it:
    public List<User> findByCompany_CompanyNameContainingIgnoreCase(String companyName);

    Optional<User> findByCompany_CompanyNameIgnoreCaseAndExternalUserId(
            String companyName,
            String externalUserId
    );

    public List<User> findByCustomerAndActive(Customer customer, int ActiveStatus);


}
