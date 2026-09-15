package com.example.varthakassesment.Model;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="Internal_STATUS")
public class Status {

    @Id
    @Column(name="Status_Id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID statusId;

    @Column(name="Status_name")
    private String statusName;

    //Status belongs to many users
    @OneToMany(mappedBy = "status")
    List<User> user = new ArrayList<>();

    public Status() {
    }

    public Status(UUID statusId, String statusName, List<User> user) {
        this.statusId = statusId;
        this.statusName = statusName;
        this.user = user;
    }

    public UUID getStatusId() {
        return statusId;
    }

    public void setStatusId(UUID statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public List<User> getUser() {
        return user;
    }

    public void setUser(List<User> user) {
        this.user = user;
    }
}


