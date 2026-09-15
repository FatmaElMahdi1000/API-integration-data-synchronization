package com.example.varthakassesment.Repo;

import com.example.varthakassesment.Model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StatusRepo extends JpaRepository<Status, UUID> {

    public Status findByStatusNameIgnoreCase(String statusName);
}
