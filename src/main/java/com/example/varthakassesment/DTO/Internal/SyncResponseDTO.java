package com.example.varthakassesment.DTO.Internal;

public class SyncResponseDTO {

    private int created;
    private int updated;
    private int deactivated;

    public SyncResponseDTO() {
    }

    public SyncResponseDTO(int created, int updated, int deactivated) {
        this.created = created;
        this.updated = updated;
        this.deactivated = deactivated;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public int getDeactivated() {
        return deactivated;
    }

    public void setDeactivated(int deactivated) {
        this.deactivated = deactivated;
    }
}
