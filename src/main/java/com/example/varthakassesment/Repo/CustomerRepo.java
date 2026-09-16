package com.example.varthakassesment.Repo;

import com.example.varthakassesment.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepo extends JpaRepository<Customer, UUID> {

//    Optional<Customer>
    public Optional<Customer> findByCustomerNameIgnoreCase(String CustomerName);

}
