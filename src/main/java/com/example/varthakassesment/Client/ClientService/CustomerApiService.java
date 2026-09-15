package com.example.varthakassesment.Client.ClientService;

import com.example.varthakassesment.DTO.External.ExUserDTO;
import com.example.varthakassesment.Response.UserListResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerApiService {

    private final RestClient restClient;

    public CustomerApiService(RestClient customerRestClient) {
        this.restClient = customerRestClient;
    }

    // Call the external API's /api/users endpoint using HTTP GET
    public UserListResponse getUsers(int page, int limit) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/users")
                        .queryParam("page", page)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(UserListResponse.class);
    }

    // Service for fetching all pages with retry logic and null safety
    public List<ExUserDTO> fetchAllPages() {
        List<ExUserDTO> allUsers = new ArrayList<>();
        int limit = 50;
        int page = 1;
        int maxRetries = 3;

        while (true) {
            UserListResponse response = null;
            int attempts = 0;

            // 1. Retry mechanism for temporary network / API failures
            while (attempts < maxRetries) {
                try {
                    response = getUsers(page, limit);
                    break; // Request succeeded, exit retry loop
                } catch (Exception ex) {
                    attempts++;
                    System.err.println("Attempt " + attempts +
                            " failed for page " + page + ": " + ex.getMessage());

                    if (attempts >= maxRetries) {
                        // Abort sync cleanly to prevent incomplete data from corrupting DB state
                        throw new RuntimeException("External API failed at page "
                                + page + " after " + maxRetries + " retries", ex);
                    }

                    try {
                        Thread.sleep(1500L * attempts); // Backoff: 1.5s, 3s before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Sync process interrupted", ie);
                    }
                }
            }

            // 2. Safe null checking on response and data list
            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                break; // No more data to fetch
            }

            allUsers.addAll(response.getData());

            // 3. Safe pagination checking
            if (response.getPagination() == null || !response.getPagination().isHasNextPage()) {
                break; // Reached the final page
            }

            page++;
        }

        return allUsers;
    }
}
