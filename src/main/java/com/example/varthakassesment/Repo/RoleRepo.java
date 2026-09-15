package com.example.varthakassesment.Repo;

import com.example.varthakassesment.Model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepo extends JpaRepository<Role, UUID> {

    public Role findByNameIgnoreCase(String roleName);
}
