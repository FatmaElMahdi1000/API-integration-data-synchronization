package com.example.varthakassesment.Controller;


import com.example.varthakassesment.DTO.Internal.SyncResponseDTO;
import com.example.varthakassesment.Model.Customer;
import com.example.varthakassesment.Response.GeneralResponse;
import com.example.varthakassesment.Service.SynchronizeUser;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/sync")
public class SyncController
{
    private final SynchronizeUser _SyncUserService;

    public SyncController(SynchronizeUser _SyncUserService) {
        this._SyncUserService = _SyncUserService;
    }

    @PostMapping("/users")
    ResponseEntity<GeneralResponse<SyncResponseDTO>> SyncUsers(@RequestBody UUID customerId)
    {

        GeneralResponse<SyncResponseDTO> response = this._SyncUserService.syncUsers(customerId);

        switch (response.getResponse())
        {
            case BAD_GATEWAY:
                return  ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
            case OK:
                return  ResponseEntity.status(HttpStatus.OK).body(response);
            default:
                return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        }

    }


}
