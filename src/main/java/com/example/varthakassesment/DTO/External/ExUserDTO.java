package com.example.varthakassesment.DTO.External;

import java.time.Instant;

public class ExUserDTO {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String status;
    private ExCompanyDTO company; // company 1 --- * USER relation
    private Instant createdAt;
    private Instant updatedAt;


    public ExUserDTO() {
    }

    public ExUserDTO(String status, String id, String name, String email, String phone, ExCompanyDTO company, Instant createdAt, Instant updatedAt) {
        this.status = status;
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ExCompanyDTO getCompany() {
        return company;
    }

    public void setCompany(ExCompanyDTO company) {
        this.company = company;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
