package com.example.varthakassesment.Services;


import com.example.varthakassesment.DTO.Internal.UserDTO;
import com.example.varthakassesment.Enum.ResponseStatus;
import com.example.varthakassesment.Model.*;
import com.example.varthakassesment.Repo.UserRepo;
import com.example.varthakassesment.Response.GeneralResponse;
import com.example.varthakassesment.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class UserServiceTest {


    @Mock
    private  UserRepo _userRepo;

    @InjectMocks
    UserService _userService; //One we test

    //fake data:
    private List<User> mockUsers;
    private User user1;
    private User user2;
    private Customer customer;
    private Role role;
    private Company company;
    private Status status;

    @BeforeEach
    void setUp() {
        mockUsers = new ArrayList<>();

        // Fake related entities
         customer = new Customer();
        customer.setCustomerID(UUID.randomUUID());

        role = new Role();
        role.setRoleId(UUID.randomUUID());

        company = new Company();
        company.setCompanyId(UUID.randomUUID());
        company.setCompanyName("ABC Corp");

        Status status = new Status();
        status.setStatusId(UUID.randomUUID());

        // Fake User 1
        user1 = new User();
        user1.setUserId(UUID.randomUUID());
        user1.setCustomer(customer);
        user1.setRole(role);
        user1.setCompany(company);
        user1.setStatus(status);
        user1.setExternalUserId("ext-001");
        user1.setName("Fatma");
        user1.setEmail("Fatma@test.com");
        user1.setPhone("1234567890");
        user1.setActive(1);

        // Fake User 2 (Nullable relationships to test safety checks)
        user2 = new User();
        user2.setUserId(UUID.randomUUID());
        user2.setExternalUserId("ext-002");
        user2.setName("M");
        user2.setEmail("M@test.com");
        user2.setPhone("0987654321");
        user2.setActive(1);

        mockUsers.add(user1);
        mockUsers.add(user2);
    }

    @Test
    void FindingALLUsers()
    {

        //List of users returned successfully
        when(this._userRepo.findAll()).thenReturn(mockUsers);

        //Act
        GeneralResponse<List<UserDTO>> response = this._userService.getAllUsers();

        //
        assertEquals(response.getData().size(), mockUsers.size());
        assertEquals(ResponseStatus.OK, response.getResponse());

        verify(this._userRepo).findAll();

    }


    @Test
    void getAllUsers_WhenEmpty_ReturnsOkWithEmptyList()
    {
        //Failed retrieving users data
        when(this._userRepo.findAll()).thenReturn(List.of()); //List.of returning empty list
        //Act
        GeneralResponse<List<UserDTO>> response = this._userService.getAllUsers();

        assertEquals(ResponseStatus.OK, response.getResponse());
        assertTrue(response.getData().isEmpty());

    }

    @Test
    void GettingErrorRetrievingUsers()
    {
        when(this._userRepo.findAll()).thenThrow(new RuntimeException("Database connection issue"));

        //Act
        GeneralResponse<List<UserDTO>> response = this._userService.getAllUsers();

        //Assert and verify
        assertEquals(ResponseStatus.INTERNAL_SERVER_ERROR, response.getResponse());
        assertNull(response.getData());
        verify(this._userRepo).findAll();
    }


    @Test
    void getUserByCompany_Success()
    {
        //when
        when(this._userRepo.findByCompany_CompanyNameContainingIgnoreCase(company.getCompanyName())).thenReturn(mockUsers);

        //Act
        GeneralResponse<List<UserDTO>> response = this._userService.getUsersByCompany( company.getCompanyName());

        assertEquals(ResponseStatus.OK,response.getResponse());
        assertEquals(mockUsers.size(), response.getData().size());

        assertNotNull(response);

        //Assert/verify //testing service / verifies mock
        verify(this._userRepo)
                .findByCompany_CompanyNameContainingIgnoreCase(company.getCompanyName()); //verification that the service used them method

    }


    @Test
    void getUserByCompany_Failure()
    {

        //when
        when(this._userRepo.findByCompany_CompanyNameContainingIgnoreCase(company.getCompanyName())).thenThrow(new RuntimeException("Failed to retrieve users by company"));

        //Act
        GeneralResponse<List<UserDTO>> response = this._userService.getUsersByCompany( company.getCompanyName());

        //Assert/verify
        assertEquals(ResponseStatus.INTERNAL_SERVER_ERROR,response.getResponse());
        assertNull(response.getData());

//        verifyNoInteractions(); //verification that the service used the mock/verify mock
        verify(this._userRepo)
                .findByCompany_CompanyNameContainingIgnoreCase(company.getCompanyName());


    }

}




