package org.jvnkr.blogbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
  private UUID id;
  private PostAuthorDto author;
  private Date createdAt;
  private String title;
  private String description;
  private boolean isLiked;
  private int likes;
  private int comments;
  private boolean isOverDescLimit;
}
