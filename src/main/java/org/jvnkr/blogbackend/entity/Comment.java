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
@Table(name = "comments")
public class Comment {
  //  @Id
//  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
//  private Long id;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "text", nullable = false)
  private String text;

  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
  private Date createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id")
  private Comment parentComment;

  @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Comment> replies = new HashSet<>();

  // Helper methods for managing bidirectional relationships
  public void addReply(Comment reply) {
    replies.add(reply);
    reply.setParentComment(this);
  }

  public void removeReply(Comment reply) {
    replies.remove(reply);
    reply.setParentComment(null);
  }
}
