package org.jvnkr.blogbackend.service;

import lombok.Getter;
import org.jvnkr.blogbackend.entity.Role;

import java.util.Comparator;
import java.util.Set;

// Using ROLE_ prefix because Spring Security adds the ROLE_ prefix when we check hasRole('ADMIN')
@Getter
public enum Roles {
  ROLE_USER(1),
  ROLE_ADMIN(2);
  private final int priority;

  Roles(int priority) {
    this.priority = priority;
  }

  public static Roles fromName(String roleName) {
    for (Roles rolePriority : values()) {
      if (rolePriority.name().equalsIgnoreCase(roleName)) {
        return rolePriority;
      }
    }
    throw new IllegalArgumentException("Unknown role: " + roleName);
  }

  public static String getHighestRole(Set<Role> roles) {
    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("Role set cannot be null or empty.");
    }

    return roles.stream()
            .max(Comparator.comparingInt(role -> Roles.fromName(role.getName()).getPriority())) // Compare priorities using Roles enum
            .map(Role::getName) // Map the highest Role to its name
            .orElseThrow(() -> new IllegalStateException("No valid roles found.")); // Throw an exception if no role is valid
  }

}
