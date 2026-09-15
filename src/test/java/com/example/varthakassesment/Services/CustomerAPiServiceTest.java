package com.example.varthakassesment.Client.ClientService;

import com.example.varthakassesment.DTO.External.ExPaginationDTO;
import com.example.varthakassesment.DTO.External.ExUserDTO;
import com.example.varthakassesment.Response.UserListResponse;
import com.example.varthakassesment.Response.UserListResponse.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerAPiServiceTest {

    @Mock
    private RestClient restClient;

    @Spy
    private CustomerApiService apiService = new CustomerApiService(restClient);

    private ExUserDTO user1;

    @BeforeEach
    void setUp() {
        user1 = new ExUserDTO();
        user1.setId("1");
        user1.setName("Fatma");
    }

    @Test
    void fetchAllPages_SinglePage_ReturnsUsers() {
        ExPaginationDTO pagination = new ExPaginationDTO();
        pagination.setHasNextPage(false);

        UserListResponse response = new UserListResponse();
        response.setData(List.of(user1));
        response.setPagination(pagination);

        doReturn(response).when(apiService).getUsers(1, 50);

        List<ExUserDTO> result = apiService.fetchAllPages();

        assertEquals(1, result.size());
        assertEquals("Fatma", result.getFirst().getName());
        verify(apiService, times(1)).getUsers(1, 50);
    }

    @Test
    void fetchAllPages_MultiplePages_AggregatesUsers() {
        ExUserDTO user2 = new ExUserDTO();
        user2.setId("2");

        ExPaginationDTO page1Pagination = new ExPaginationDTO();
        page1Pagination.setHasNextPage(true);

        UserListResponse page1Response = new UserListResponse();
        page1Response.setData(List.of(user1));
        page1Response.setPagination(page1Pagination);

        ExPaginationDTO page2Pagination = new ExPaginationDTO();
        page2Pagination.setHasNextPage(false);

        UserListResponse page2Response = new UserListResponse();
        page2Response.setData(List.of(user2));
        page2Response.setPagination(page2Pagination);

        doReturn(page1Response).when(apiService).getUsers(1, 50);
        doReturn(page2Response).when(apiService).getUsers(2, 50);

        List<ExUserDTO> result = apiService.fetchAllPages();

        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("2", result.get(1).getId());
        verify(apiService, times(1)).getUsers(1, 50);
        verify(apiService, times(1)).getUsers(2, 50);
    }
}