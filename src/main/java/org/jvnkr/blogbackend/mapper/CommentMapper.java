package org.jvnkr.blogbackend.mapper;

import org.jvnkr.blogbackend.dto.CommentDto;
import org.jvnkr.blogbackend.dto.PostAuthorDto;
import org.jvnkr.blogbackend.dto.RepliesToAuthorDto;
import org.jvnkr.blogbackend.entity.Comment;
import org.jvnkr.blogbackend.entity.User;

public class CommentMapper {
  public static CommentDto toCommentDto(Comment comment, User user) {
    return new CommentDto(
            comment.getId(),
            comment.getParentComment() != null ? comment.getParentComment().getId() : null,
            comment.getRepliesToComment() != null ?
                    new RepliesToAuthorDto(
                            comment.getRepliesToComment().getId(),
                            comment.getRepliesToComment().getUser().getUsername()
                    ) : null,
            comment.getText(),
            comment.getLikedBy().contains(user),
            comment.getLikedBy().size(),
            comment.getReplies().size(),
            comment.getCreatedAt(),
            new PostAuthorDto(
                    comment.getUser().getId(),
                    comment.getUser().getName(),
                    comment.getUser().getUsername(),
                    comment.getUser().isVerified()
            )
    );
  }
}
