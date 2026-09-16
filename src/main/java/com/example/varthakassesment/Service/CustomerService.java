package com.example.varthakassesment.Service;


import com.example.varthakassesment.DTO.Internal.CustomerDTO;
import com.example.varthakassesment.Enum.ResponseStatus;
import com.example.varthakassesment.Model.Customer;
import com.example.varthakassesment.Repo.CustomerRepo;
import com.example.varthakassesment.Response.GeneralResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {


    private final CustomerRepo customerRepo;

    public CustomerService(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    public GeneralResponse<CustomerDTO> AddCustomer(CustomerDTO newCustomer)


    {
        try {
            //Check if customer is already in the Database:
            Optional<Customer> ExistingCustomer = this.customerRepo.findByCustomerNameIgnoreCase
                    (newCustomer.getCustomerName());

            if (ExistingCustomer.isEmpty()) {

                Customer customer = new Customer();

                customer.setCustomerName(newCustomer.getCustomerName());

                Customer savedCustomer = this.customerRepo.save(customer);

                return new GeneralResponse<>(ResponseStatus.CREATED, "Customer has been created successfully", new CustomerDTO(savedCustomer.getCustomerID(), savedCustomer.getCustomerName()));

            } else {
                return new GeneralResponse<>(ResponseStatus.BAD_REQUEST, "Customer is present in the database", null);

            }
        }
        catch (Exception ex)
        {

            return new GeneralResponse<>
                    ( ResponseStatus.INTERNAL_SERVER_ERROR, null, null);

        }

    }



}
