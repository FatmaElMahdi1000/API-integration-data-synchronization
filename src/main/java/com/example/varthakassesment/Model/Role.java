package com.example.varthakassesment.Model;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="InternalROLE")
public class Role {

    @Id
    @Column(name="role_Id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID roleId;

    @Column(name="name")
    private String name;

    //Role has 0,1,many users

    @OneToMany(mappedBy="role")
    private List<User> users = new ArrayList<>(); //avoid seeing null, [] appears an empty

    public Role() {
    }

    public Role(UUID roleId, String name, List<User> users) {
        this.roleId = roleId;
        this.name = name;
        this.users = users;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
