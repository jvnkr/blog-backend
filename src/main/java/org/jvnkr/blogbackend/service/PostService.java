package org.jvnkr.blogbackend.service;

import org.jvnkr.blogbackend.dto.PostDto;

import java.util.List;
import java.util.UUID;

public interface PostService {
  String createPost(String title, String description, UUID userId);

  boolean likePost(UUID postId, UUID userId);

  boolean unlikePost(UUID postId, UUID userId);

  PostDto getPostById(UUID postId);

  List<PostDto> getAllPosts();

  String updatePost(UUID postId, UUID userId, String newTitle, String newDescription);

  String deletePost(UUID postId, UUID userId);

  List<PostDto> getBatchOfUserPosts(UUID userId, int pageNumber, int batchSize, UUID viewerId);

  List<PostDto> getBatchOfAllPosts(int pageNumber, int batchSize, UUID viewerId);
}
