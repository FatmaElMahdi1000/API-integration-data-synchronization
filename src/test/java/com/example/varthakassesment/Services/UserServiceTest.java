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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepo _userRepo;

    @InjectMocks
    private UserService _userService;

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

        customer = new Customer();
        customer.setCustomerID(UUID.randomUUID());

        role = new Role();
        role.setRoleId(UUID.randomUUID());

        company = new Company();
        company.setCompanyId(UUID.randomUUID());
        company.setCompanyName("ABC Corp");

        status = new Status();
        status.setStatusId(UUID.randomUUID());

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
    void FindingALLUsers() {
        when(this._userRepo.findByCustomer_CustomerID(customer.getCustomerID())).thenReturn(mockUsers);

        GeneralResponse<List<UserDTO>> response = this._userService.getAllUsers(customer.getCustomerID());

        assertEquals(mockUsers.size(), response.getData().size());
        assertEquals(ResponseStatus.OK, response.getResponse());

        verify(this._userRepo).findByCustomer_CustomerID(customer.getCustomerID());
    }

    @Test
    void getAllUsers_WhenEmpty_ReturnsOkWithEmptyList() {
        when(this._userRepo.findByCustomer_CustomerID(customer.getCustomerID())).thenReturn(List.of());

        GeneralResponse<List<UserDTO>> response = this._userService.getAllUsers(customer.getCustomerID());

        assertEquals(ResponseStatus.OK, response.getResponse());
        assertTrue(response.getData().isEmpty());

        verify(this._userRepo).findByCustomer_CustomerID(customer.getCustomerID());
    }

    @Test
    void GettingErrorRetrievingUsers() {
        when(this._userRepo.findByCustomer_CustomerID(customer.getCustomerID()))
                .thenThrow(new RuntimeException("Database connection issue"));

        GeneralResponse<List<UserDTO>> response = this._userService.getAllUsers(customer.getCustomerID());

        assertEquals(ResponseStatus.INTERNAL_SERVER_ERROR, response.getResponse());
        assertNull(response.getData());

        verify(this._userRepo).findByCustomer_CustomerID(customer.getCustomerID());
    }

    @Test
    void getUserByCompany_Success() {
        when(this._userRepo.findByCustomer_CustomerIDAndCompany_CompanyNameContainingIgnoreCase(
                customer.getCustomerID(), company.getCompanyName()))
                .thenReturn(mockUsers);

        GeneralResponse<List<UserDTO>> response = this._userService.getUsersByCompany(
                customer.getCustomerID(), company.getCompanyName());

        assertNotNull(response);
        assertEquals(ResponseStatus.OK, response.getResponse());
        assertEquals(mockUsers.size(), response.getData().size());

        verify(this._userRepo).findByCustomer_CustomerIDAndCompany_CompanyNameContainingIgnoreCase(
                customer.getCustomerID(), company.getCompanyName());
    }
}