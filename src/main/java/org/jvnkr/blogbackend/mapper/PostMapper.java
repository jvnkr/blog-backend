package org.jvnkr.blogbackend.mapper;

import org.jvnkr.blogbackend.dto.PostAuthorDto;
import org.jvnkr.blogbackend.dto.PostDto;
import org.jvnkr.blogbackend.entity.Post;
import org.jvnkr.blogbackend.entity.User;

public class PostMapper {
  private static final int DESC_CHAR_LIMIT = 250;

  public static PostDto toPreviewPostDto(Post post, User user) {
    User postAuthor = post.getUser();
    return new PostDto(
            post.getId(),
            new PostAuthorDto(
                    postAuthor.getId(),
                    postAuthor.getName(),
                    postAuthor.getUsername(),
                    postAuthor.isVerified()
            ),
            post.getCreatedAt(),
            post.getTitle(),
            post.getDescription().length() > DESC_CHAR_LIMIT ?
                    post.getDescription().substring(0, DESC_CHAR_LIMIT) : post.getDescription(),
            post.getLikedBy().contains(user),
            post.getLikedBy().size(),
            post.getTopLevelComments().size(),
            post.getDescription().length() > DESC_CHAR_LIMIT
    );
  }

  public static PostDto toPostDto(Post post, User user) {
    User postAuthor = post.getUser();
    return new PostDto(
            post.getId(),
            new PostAuthorDto(
                    postAuthor.getId(),
                    postAuthor.getName(),
                    postAuthor.getUsername(),
                    postAuthor.isVerified()
            ),
            post.getCreatedAt(),
            post.getTitle(),
            post.getDescription(),
            post.getLikedBy().contains(user),
            post.getLikedBy().size(),
            post.getTopLevelComments().size(),
            false
    );
  }
}
