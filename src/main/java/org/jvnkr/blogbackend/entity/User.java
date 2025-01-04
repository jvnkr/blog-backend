package org.jvnkr.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id; // Primary key

  @Column(name = "name", nullable = false, length = 70)
  private String name;

  @Column(name = "username", unique = true, nullable = false, length = 20)
  private String username;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "email", unique = true, nullable = false)
  private String email;

  @Column(name = "bio", columnDefinition = "TEXT")
  private String bio;

  @Column(name = "deleted")
  private boolean deleted;

  @Column(name = "verified")
  private boolean verified;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Date updatedAt;

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(
          name = "users_roles",
          joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
  )
  private Set<Role> roles = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Post> posts = new HashSet<>();

  // Followers list (users that follow this user)
  @ManyToMany
  @JoinTable(
          name = "user_followers",
          joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "follower_id", referencedColumnName = "id")
  )
  private Set<User> followers = new HashSet<>();

  // Following list (users this user follows)
  @ManyToMany(mappedBy = "followers")
  private Set<User> following = new HashSet<>();

  /**
   * Override equals and hashCode to ensure uniqueness.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;
    return id != null && id.equals(((User) o).id); // Compare by unique ID only
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}