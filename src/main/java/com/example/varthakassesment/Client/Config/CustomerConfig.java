package com.example.varthakassesment.Client.Config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class CustomerConfig {

    //RestClient is a Spring class that allows your Java application to make HTTP requests to another application/API.
    @Bean
    public RestClient customerRestClient(
            @Value("${customer.api.base-url}") String baseUrl,
            @Value("${customer.api.token}") String token) {

        //how iam getting authentication
        // accessing the customer API:putting it into: HTTP header format
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .build();
    }

    //purpose converting ro http header format where the user already got authenticated
    //HOW I KNOW IF THIS USER REALLY AUTHENTICATED ?????

}
