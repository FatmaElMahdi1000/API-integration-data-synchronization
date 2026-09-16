package com.example.varthakassesment.Repo;

import com.example.varthakassesment.Model.Company;
import com.example.varthakassesment.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepo extends JpaRepository<Company, UUID> {

    //Added:
    Company findByCustomerAndCompanyNameIgnoreCase(Customer customer, String CompanyName);

    Company findByCompanyNameIgnoreCase(String companyName);
}
