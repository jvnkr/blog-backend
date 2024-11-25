package org.jvnkr.blogbackend.mapper;

import org.jvnkr.blogbackend.dto.CommentDto;
import org.jvnkr.blogbackend.entity.Comment;

public class CommentMapper {
  public static CommentDto toCommentDto(Comment comment) {
    return new CommentDto(
            comment.getText()
    );
  }
}
