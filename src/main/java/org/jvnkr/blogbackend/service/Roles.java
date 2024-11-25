package org.jvnkr.blogbackend.service;

// Using ROLE_ prefix because Spring Security adds the ROLE_ prefix when we check hasRole('ADMIN')
public enum Roles {
  ROLE_USER,
  ROLE_ADMIN
}
