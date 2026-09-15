package com.example.varthakassesment.DTO.Internal;

import com.example.varthakassesment.Model.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoleDTO {

    private UUID roleId;

    private String name;

    private List<UUID> usersIds = new ArrayList<>(); //avoid seeing null, [] appears an empty

    public RoleDTO() {
    }

    //Without Ids
    public RoleDTO(String name) {
        this.name = name;
    }

    public RoleDTO(UUID roleId, String name, List<UUID> usersIds) {
        this.roleId = roleId;
        this.name = name;
        this.usersIds = usersIds;
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

    public List<UUID> getUsersIds() {
        return usersIds;
    }

    public void setUsersIds(List<UUID> usersIds) {
        this.usersIds = usersIds;
    }
}
