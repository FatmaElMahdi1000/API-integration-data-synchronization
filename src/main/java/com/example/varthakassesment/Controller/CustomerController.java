package com.example.varthakassesment.Controller;


import com.example.varthakassesment.DTO.Internal.CustomerDTO;
import com.example.varthakassesment.Response.GeneralResponse;
import com.example.varthakassesment.Service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/customers")
public class CustomerController {


    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<GeneralResponse<CustomerDTO>> addNewCustomer(
            @RequestBody CustomerDTO newCustomer) {

        GeneralResponse<CustomerDTO> response =
                this.customerService.AddCustomer(newCustomer);

        switch (response.getResponse()) {

            case CREATED:
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(response);

            case BAD_REQUEST:
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);

            default:
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(response);
        }
    }


}
