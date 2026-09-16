package com.example.varthakassesment.Services;

import com.example.varthakassesment.Client.ClientService.CustomerApiService;
import com.example.varthakassesment.DTO.External.ExCompanyDTO;
import com.example.varthakassesment.DTO.External.ExUserDTO;
import com.example.varthakassesment.DTO.Internal.SyncResponseDTO;
import com.example.varthakassesment.Enum.ResponseStatus;
import com.example.varthakassesment.Mapper.UserMapper;
import com.example.varthakassesment.Model.Customer;
import com.example.varthakassesment.Model.User;
import com.example.varthakassesment.Repo.*;
import com.example.varthakassesment.Response.GeneralResponse;
import com.example.varthakassesment.Service.SynchronizeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SynchronizeUserServiceTest {

    @Mock
    CustomerApiService _ApiService;
    @Mock
    UserRepo _userRepo;
    @Mock
    CustomerRepo _customerRepo;
    @Mock
    CompanyRepo _companyRepo;
    @Mock
    StatusRepo _statusRepo;
    @Mock
    RoleRepo _roleRepo;
    @Mock
    UserMapper userMapper;

    @InjectMocks
    SynchronizeUser _OriginalSyncService;

    private Customer customer;
    private UUID customerId;
    private List<ExUserDTO> externalUsers;
    private ExUserDTO externalUser1;

    @BeforeEach
    void CustomerInfo() {
        customer = new Customer();
        this.customerId = UUID.fromString("40ac987a-f34b-43dc-9a1a-1d5e52d662c3");
        customer.setCustomerID(customerId);
        customer.setCustomerName("gamma");

        ExCompanyDTO company = new ExCompanyDTO();
        company.setName("ABC");
        company.setRole("Developer");

        externalUsers = new ArrayList<>();

        externalUser1 = new ExUserDTO();
        externalUser1.setId("external-001");
        externalUser1.setCompany(company);
        externalUser1.setName("Fatma");
        externalUser1.setEmail("fatma@test.com");
        externalUser1.setPhone("01000000000");
        externalUser1.setStatus("active");

        externalUsers.add(externalUser1);
    }

    @Test
    void CustomerExists() {
        when(_customerRepo.findById(customerId)).thenReturn(Optional.of(customer));

        _OriginalSyncService.syncUsers(customerId);

        verify(this._customerRepo).findById(customerId);
    }

    @Test
    void CustomerDoesNotExist_ReturnsNotFound() {
        when(this._customerRepo.findById(customerId)).thenReturn(Optional.empty());

        GeneralResponse<SyncResponseDTO> response = this._OriginalSyncService.syncUsers(customerId);

        assertEquals(ResponseStatus.NOT_FOUND, response.getResponse());
        assertEquals("Customer not found", response.getMessage());
        assertNull(response.getData());

        verifyNoInteractions(this._ApiService, this._userRepo);
    }

    @Test
    void ApiFailure() {
        when(_customerRepo.findById(customerId)).thenReturn(Optional.of(customer));
        when(_ApiService.fetchAllPages()).thenThrow(new RuntimeException("API Timeout"));

        GeneralResponse<SyncResponseDTO> response = _OriginalSyncService.syncUsers(customerId);

        assertEquals(ResponseStatus.BAD_GATEWAY, response.getResponse());
        assertNull(response.getData());
        verifyNoInteractions(_userRepo);
    }

    @Test
    void updateExistingUsers_Success() {
        User existingUser = new User();
        existingUser.setExternalUserId("external-001");
        existingUser.setActive(1);

        // Set two distinct Instant values using Instant.parse
        existingUser.setExternalUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        externalUser1.setUpdatedAt(Instant.parse("2026-01-02T00:00:00Z"));

        when(this._customerRepo.findById(customerId)).thenReturn(Optional.of(customer));
        when(this._ApiService.fetchAllPages()).thenReturn(externalUsers);
        when(this._userRepo.findByCustomerAndExternalUserId(customer, "external-001"))
                .thenReturn(Optional.of(existingUser));
        when(this._userRepo.findByCustomerAndActive(customer, 1))
                .thenReturn(List.of(existingUser));

        GeneralResponse<SyncResponseDTO> response = this._OriginalSyncService.syncUsers(customerId);

        assertEquals(ResponseStatus.OK, response.getResponse());
        assertEquals(0, response.getData().getCreated());
        assertEquals(1, response.getData().getUpdated());
        assertEquals(0, response.getData().getDeactivated());

        verify(this.userMapper).MapExUserToEntity(externalUser1, existingUser);
        verify(this._userRepo).save(existingUser);
    }

    @Test
    void deactivatesMissingUsers() {
        User user = new User();
        user.setExternalUserId("external-fake");
        user.setActive(1);

        when(this._customerRepo.findById(customerId)).thenReturn(Optional.of(customer));
        when(this._ApiService.fetchAllPages()).thenReturn(externalUsers);
        when(this._userRepo.findByCustomerAndExternalUserId(customer, "external-001"))
                .thenReturn(Optional.empty());
        when(this._userRepo.findByCustomerAndActive(customer, 1))
                .thenReturn(List.of(user));

        GeneralResponse<SyncResponseDTO> response = this._OriginalSyncService.syncUsers(customerId);

        assertEquals(ResponseStatus.OK, response.getResponse());
        assertEquals(1, response.getData().getCreated());
        assertEquals(0, response.getData().getUpdated());
        assertEquals(1, response.getData().getDeactivated());

        assertEquals(0, user.getActive());
        verify(this._userRepo).save(user);
    }
}