package com.example.varthakassesment.DTO.Internal;

import com.example.varthakassesment.Model.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatusDTO {

    private UUID statusId;

    private String statusName;

    List<UUID> userIds = new ArrayList<>();


    public StatusDTO() {
    }

    public StatusDTO(UUID statusId, String statusName, List<UUID> userIds) {
        this.statusId = statusId;
        this.statusName = statusName;
        this.userIds = userIds;
    }

    //Without Ids
    public StatusDTO(String statusName) {
        this.statusName = statusName;
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

    public List<UUID> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<UUID> userIds) {
        this.userIds = userIds;
    }
}
