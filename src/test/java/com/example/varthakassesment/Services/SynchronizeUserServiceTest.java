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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SynchronizeUserServiceTest
{

    //Mock for
    @Mock //fetchAllPages() is currently implemented inside this service, so I mocked it
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
    SynchronizeUser _OriginalSyncService; //we're testing this not testing the mocks

    // Shared test data
    private Customer customer;
    private UUID customerId;
    private List<ExUserDTO> externalUsers;
    private ExUserDTO externalUser1;

    //Creating test data
    @BeforeEach
    void CustomerInfo()
    {
        customer = new Customer();

        this.customerId = UUID.fromString("40ac987a-f34b-43dc-9a1a-1d5e52d662c3");

        customer.setCustomerID(customerId);
        customer.setCustomerName("gamma");

        ExCompanyDTO company = new ExCompanyDTO();
        company.setName("ABC");
        company.setRole("Developer");

        //some external users info :(pretend getting it fetched from an external API)
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


    //Customer Found
    @Test
    void CustomerExists()
    {
        //before each annotation will get customer from the mock data base
        //when this method call customer must get returned
        when(_customerRepo.findById(customerId))
                .thenReturn(Optional.of(customer));

        //act  application using our method we're using : Sync user
        _OriginalSyncService.syncUsers(customerId);
        //assert:

        //"Assert that during this test run, the service actually
        // called _customerRepo.findById(...) exactly once using this specific customerId
        //1.verifying if the service: syncUsers acted as expected and called the method to get the user:
        verify(this._customerRepo).findById(customerId);
    }


    //customer is not found
    @Test
    void CustomerDoesNotExist_throwException()
    {
        //act
        when(this._customerRepo.findById(customerId)).thenReturn(Optional.empty());

        //assert

        assertThrows(RuntimeException.class, () -> {this._OriginalSyncService.syncUsers(customerId);
        });

        //ensuring no  API call if customer does not exist: //one or more mock objects can get passed, ensure no methods are
        //executing on them
        verifyNoInteractions(this._ApiService, this._userRepo);

    }

//Test of : External API failing:

    @Test
    void ApiFailure()
    {
       //when
        when(_customerRepo.findById(customerId)).thenReturn(Optional.of(customer)); //customer will return ok
        when(_ApiService.fetchAllPages()).thenThrow(new RuntimeException("API Timeout"));
         //act
        GeneralResponse<SyncResponseDTO> response = _OriginalSyncService.syncUsers(customerId);

        //asse
        assertEquals(ResponseStatus.BAD_GATEWAY, response.getResponse());
        assertNull(response.getData());
        verifyNoInteractions(_userRepo); // won't do any storing to the data base


    }


    //test of Updating Existing Users/saving means customer found, we could establish connection with the API successfully:

    @Test
    void updateExistingUsers_Success() {
        User existingUser = new User();
        existingUser.setExternalUserId("external-001");

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

    //We need to simulate a user who currently exists as active (active = 1) in your local database
    //whose ID is missing from the list returned by the external API

    //REVIEW
    @Test
    void deactivatesMissingUsers() {

        // in our local database - currently active
        User user = new User();
        user.setExternalUserId("external-fake");
        user.setActive(1);

        when(this._customerRepo.findById(customerId)).thenReturn(Optional.of(customer));

        // API returns externalUsers only
        when(this._ApiService.fetchAllPages()).thenReturn(externalUsers);

        // Mock incoming user check (external-001 is a new user)
        when(this._userRepo.findByCustomerAndExternalUserId(customer, "external-001"))
                .thenReturn(Optional.empty());

        // CRITICAL: Return the stale user when querying active users from the database
        when(this._userRepo.findByCustomerAndActive(customer, 1))
                .thenReturn(List.of(user));

        // ACT
        GeneralResponse<SyncResponseDTO> response = this._OriginalSyncService.syncUsers(customerId);

        // ASSERT
        assertEquals(ResponseStatus.OK, response.getResponse());
        assertEquals(1, response.getData().getCreated());     // external-001 created
        assertEquals(0, response.getData().getUpdated());     // 0 updated
        assertEquals(1, response.getData().getDeactivated()); // deactivated

        // Verify  user was marked inactive and saved
        assertEquals(0, user.getActive());
        verify(this._userRepo).save(user);
    }






    //    void ApiTestingFetchingUsers()
//    {
//
//        when(_ApiService.fetchAllPages())
//                .thenReturn(externalUsers);
//
//        when(_userRepo.findByCustomerAndExternalUserId(
//                eq(customer), //eq must equal
//                anyString() //any string as the extrenal user id in the client's platform saved as String not UUID
//        )).thenReturn(Optional.empty());  // ,find method returns optional
//
//
//        // ACT
//
//        GeneralResponse<SyncResponseDTO> response =
//                _OriginalSyncService.syncUsers(customerId);
//
//
//        // ASSERT
//
//        verify(_customerRepo).findById(customerId);
//
//        verify(_ApiService).fetchAllPages();
//
//        verify(_userRepo, times(4))
//                .save(any(User.class)); //4 users / new users created and saved :
//
//        assertEquals(ResponseStatus.OK, response.getResponse());
//
//        assertEquals(4, response.getData().getCreated());
//        //customer does not exist:
//        assertThrows(
//                RuntimeException.class,
//                () -> _OriginalSyncService.syncUsers(customerId)
//        ); //expecting to throw an exception
//        verify(_ApiService, never()).fetchAllPages(); //EXPECT THIS NEVER CALLED DUE TO AN ISSUE/ FAILED TO REACH api FOR EXAMPLE
//
//
//
//    }







}
