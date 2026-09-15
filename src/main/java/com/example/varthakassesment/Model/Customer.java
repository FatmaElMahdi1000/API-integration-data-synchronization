package com.example.varthakassesment.Model;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="Internal_Customer")
public class Customer {

    @Id
    @Column(name="Customer_Id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID customerID;

    @Column(name="Customer_name")
    private String customerName;


    //one customer - many users
    @OneToMany(mappedBy = "customer")
    List<User> users = new ArrayList<>();

    //One Customer has many companies
    @OneToMany(mappedBy = "customer")
    List<Company> companies = new ArrayList<>();




    public Customer() {
    }

    public Customer(UUID customerID, String customerName, List<User> users, List<Company> companies) {
        this.customerID = customerID;
        this.customerName = customerName;
        this.users = users;
        this.companies = companies;
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

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }
}
