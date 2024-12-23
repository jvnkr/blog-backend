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
public class CommentDto {
  private UUID id;
  private UUID rootId;
  private RepliesToAuthorDto repliesTo;
  private String text;
  private boolean isLiked;
  private int likes;
  private int replies;
  private Date createdAt;
  private PostAuthorDto author;
}
