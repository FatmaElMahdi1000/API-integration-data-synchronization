package com.example.varthakassesment.DTO.Internal;

import com.example.varthakassesment.Model.Customer;
import com.example.varthakassesment.Model.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CompanyDTO {

    private UUID Company_id;

    private String companyName;

    private String Industry;

    private String Website;

    private int employeesNumber;

    private UUID customerId;

    List<UUID> usersIds = new ArrayList<>();


    public CompanyDTO() {
    }

    //Without ID and without List

    public CompanyDTO(String companyName, String industry, String website, int employeesNumber, UUID customerId) {
        this.companyName = companyName;
        Industry = industry;
        Website = website;
        this.employeesNumber = employeesNumber;
        this.customerId = customerId;
    }

    // without List          only


    public CompanyDTO(UUID company_id, String companyName, String industry, String website, int employeesNumber, UUID customerId) {
        Company_id = company_id;
        this.companyName = companyName;
        Industry = industry;
        Website = website;
        this.employeesNumber = employeesNumber;
        this.customerId = customerId;
    }


    public CompanyDTO(UUID company_id, String companyName, String industry, int employeesNumber, String website, UUID customerId, List<UUID> usersIds) {
        Company_id = company_id;
        this.companyName = companyName;
        Industry = industry;
        this.employeesNumber = employeesNumber;
        Website = website;
        this.customerId = customerId;
        this.usersIds = usersIds;
    }


    public UUID getCompany_id() {
        return Company_id;
    }

    public void setCompany_id(UUID company_id) {
        Company_id = company_id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustry() {
        return Industry;
    }

    public void setIndustry(String industry) {
        Industry = industry;
    }

    public String getWebsite() {
        return Website;
    }

    public void setWebsite(String website) {
        Website = website;
    }

    public int getEmployeesNumber() {
        return employeesNumber;
    }

    public void setEmployeesNumber(int employeesNumber) {
        this.employeesNumber = employeesNumber;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public List<UUID> getUsersIds() {
        return usersIds;
    }

    public void setUsersIds(List<UUID> usersIds) {
        this.usersIds = usersIds;
    }
}
