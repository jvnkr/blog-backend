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
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posts")
public class Post {
  //  @Id
//  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
//  private Long id;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "title", nullable = false, length = 50)
  private String title;

  @Column(name = "description", nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
  private Date createdAt;

  @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
  private Date updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Comment> comments = new HashSet<>();

  // Getter for top-level comments (excluding replies)
  public Set<Comment> getTopLevelComments() {
    // Return comments where parentComment is null
    return comments.stream()
            .filter(comment -> comment.getParentComment() == null)
            .collect(Collectors.toSet());
  }

  // Getter for total number of all comments (including replies)
  public int getTotalComments() {
    return comments.size();
  }

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "post_likes",
          joinColumns = @JoinColumn(name = "post_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id")
  )
  private Set<User> likedBy = new HashSet<>();

  // Helper methods for managing bidirectional relationships
  public void addComment(Comment comment) {
    comments.add(comment);
    comment.setPost(this);
  }

  public void removeComment(Comment comment) {
    comments.remove(comment);
    comment.setPost(null);
  }

  // Helper methods for managing likes
  public void addLike(User user) {
    likedBy.add(user);
  }

  public void removeLike(User user) {
    likedBy.remove(user);
  }
}