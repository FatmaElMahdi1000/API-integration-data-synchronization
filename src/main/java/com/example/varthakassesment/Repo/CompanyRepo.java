package com.example.varthakassesment.Repo;

import com.example.varthakassesment.Model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepo extends JpaRepository<Company, UUID> {


    Company findByCompanyNameIgnoreCase(String companyName);
}
