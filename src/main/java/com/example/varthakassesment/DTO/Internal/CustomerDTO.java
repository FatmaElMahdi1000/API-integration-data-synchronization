package com.example.varthakassesment.DTO.Internal;

import com.example.varthakassesment.Model.Company;
import com.example.varthakassesment.Model.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomerDTO {

    private UUID customerID;

    private String customerName;

    List<UUID> usersIds= new ArrayList<>();

    List<UUID> companiesIds = new ArrayList<>();

    public CustomerDTO() {
    }

    //this is for inserting / adding input when needed to swagger:
    public CustomerDTO(String customerName) {
        this.customerName = customerName;;
    }


    //id, name, url
    public CustomerDTO(UUID customerID, String customerName) {
        this.customerName = customerName;

        this.customerID = customerID;
    }

    public CustomerDTO(UUID customerID, String customerName,  List<UUID> usersIds, List<UUID> companiesIds) {
        this.customerID = customerID;
        this.customerName = customerName;

        this.usersIds = usersIds;
        this.companiesIds = companiesIds;
    }

    public UUID getCustomerID() {
        return customerID;
    }

    public void setCustomerID(UUID customerID) {
        this.customerID = customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }


    public List<UUID> getUsersIds() {
        return usersIds;
    }

    public void setUsersIds(List<UUID> usersIds) {
        this.usersIds = usersIds;
    }

    public List<UUID> getCompaniesIds() {
        return companiesIds;
    }

    public void setCompaniesIds(List<UUID> companiesIds) {
        this.companiesIds = companiesIds;
    }







}
