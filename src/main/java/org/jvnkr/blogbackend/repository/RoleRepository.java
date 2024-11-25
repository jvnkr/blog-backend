package org.jvnkr.blogbackend.repository;

import org.jvnkr.blogbackend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
  Role findByName(String name);
}
