package com.example.varthakassesment.Controller;

import com.example.varthakassesment.DTO.Internal.UserDTO;
import com.example.varthakassesment.Response.GeneralResponse;
import com.example.varthakassesment.Service.UserService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private UserService _UserService;

    public UserController(UserService userService) {
        this._UserService = userService;
    }

//When company is null or empty (meaning the client requested GET /api/v1/users without adding  company
    @GetMapping
    public ResponseEntity<GeneralResponse<List<UserDTO>>> getUsers(
            @RequestParam(required = false) String company) {

        GeneralResponse<List<UserDTO>> response;

        if (company == null || company.isBlank()) {
            response = this._UserService.getAllUsers();
        } else {
            response = this._UserService.getUsersByCompany(company);
        }

        switch (response.getResponse()) {
            case OK:
                return ResponseEntity.status(HttpStatus.OK).body(response);
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}








