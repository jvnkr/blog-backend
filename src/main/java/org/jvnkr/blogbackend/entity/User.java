package org.jvnkr.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
  //  @Id
//  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
//  private Long id;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name", nullable = false, length = 70)
  private String name;

  @Column(name = "username", unique = true, nullable = false, length = 20)
  private String username;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "email", unique = true, nullable = false)
  private String email;

  @Column(name = "deleted")
  private boolean deleted;

  @Column(name = "verified")
  private boolean verified;

  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
  private Date createdAt;

  @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
  private Date updatedAt;

  @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
  @JoinTable(name = "users_roles",
          joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
  )
  private Set<Role> roles;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Post> posts = new HashSet<>();

  @ManyToMany
  @JoinTable(name = "user_followers",
          joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "follower_id", referencedColumnName = "id")
  )
  private Set<User> followers = new HashSet<>();

  @ManyToMany
  @JoinTable(name = "user_followers",
          joinColumns = @JoinColumn(name = "follower_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id")
  )
  private Set<User> following = new HashSet<>();
}