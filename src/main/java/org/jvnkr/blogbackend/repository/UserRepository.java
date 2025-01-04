package org.jvnkr.blogbackend.repository;

import jakarta.persistence.LockModeType;
import org.jvnkr.blogbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  // Methods like findByUsername and findByEmail leverage Spring Data JPA's query derivation mechanism,
  // where the framework parses the method names and constructs queries appropriately.
  Boolean existsByUsername(String username);

  Optional<User> findByUsernameOrEmail(String username, String email);

  Boolean existsByEmail(String email);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT u FROM User u WHERE u.id = :userId")
  Optional<User> findByIdWithLock(@Param("userId") UUID userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT u FROM User u WHERE u.email = :email OR u.username = :username")
  Optional<User> findByUsernameOrEmailWithLock(@Param("username") String username, @Param("email") String email);

  // We can do these queries for complex needs or performance optimizations
  // There are also JPA Specifications which allow further customization of the querying without writing much SQL
//  @Query("SELECT u FROM User u WHERE u.username = :username")
//  Optional<User> findByUsername(@Param("username") String username);
//
//  @Query("SELECT u FROM User u WHERE u.email = :email")
//  Optional<User> findByEmail(@Param("email") String email);
}

