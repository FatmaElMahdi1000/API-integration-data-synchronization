package com.example.varthakassesment.DTO.Internal;

import com.example.varthakassesment.Model.Company;
import com.example.varthakassesment.Model.Customer;
import com.example.varthakassesment.Model.Role;
import com.example.varthakassesment.Model.Status;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

public class UserDTO {


    private UUID userId;

    private UUID customerId;

    private UUID roleId;

    private UUID companyId;

    private UUID statusId;

    private String externalUserId;

    private String name;

    private String email;

    private String phone;

    private Instant externalCreatedAt;

    private Instant externalUpdatedAt;

    private int active = 1;

    public UserDTO() {
    }


    //Without any IDs
    public UserDTO(String externalUserId, String name, String email, String phone, Instant externalCreatedAt, Instant externalUpdatedAt, int active) {
        this.externalUserId = externalUserId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.externalCreatedAt = externalCreatedAt;
        this.externalUpdatedAt = externalUpdatedAt;
        this.active = active;
    }


    public UserDTO(UUID userId, UUID customerId, UUID roleId, UUID companyId, UUID statusId, String externalUserId, String name, String email, String phone, Instant externalCreatedAt, Instant externalUpdatedAt, int active) {
        this.userId = userId;
        this.customerId = customerId;
        this.roleId = roleId;
        this.companyId = companyId;
        this.statusId = statusId;
        this.externalUserId = externalUserId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.externalCreatedAt = externalCreatedAt;
        this.externalUpdatedAt = externalUpdatedAt;
        this.active = active;
    }


    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public UUID getStatusId() {
        return statusId;
    }

    public void setStatusId(UUID statusId) {
        this.statusId = statusId;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Instant getExternalCreatedAt() {
        return externalCreatedAt;
    }

    public void setExternalCreatedAt(Instant externalCreatedAt) {
        this.externalCreatedAt = externalCreatedAt;
    }

    public Instant getExternalUpdatedAt() {
        return externalUpdatedAt;
    }

    public void setExternalUpdatedAt(Instant externalUpdatedAt) {
        this.externalUpdatedAt = externalUpdatedAt;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }
}
