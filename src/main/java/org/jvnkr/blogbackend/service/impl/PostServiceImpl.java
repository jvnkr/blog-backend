package org.jvnkr.blogbackend.service.impl;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.PostDto;
import org.jvnkr.blogbackend.entity.Post;
import org.jvnkr.blogbackend.entity.User;
import org.jvnkr.blogbackend.exception.APIException;
import org.jvnkr.blogbackend.mapper.PostMapper;
import org.jvnkr.blogbackend.repository.PostRepository;
import org.jvnkr.blogbackend.repository.UserRepository;
import org.jvnkr.blogbackend.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PostServiceImpl implements PostService {
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  @Override
  public List<PostDto> getAllPosts() {
    List<Post> posts = postRepository.findAll();
    return posts.stream().map(PostMapper::toPostDto).collect(Collectors.toList());
  }

  @Override
  public PostDto createPost(String title, String description, UUID userId) {
    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: user is required");
    }

    User user = userOpt.get(); // Get the actual user entity from the userId

    if (title == null || title.isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: title is required");
    }
    if (description == null || description.isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: description is required");
    }
    Post newPost = new Post();
    newPost.setTitle(title);
    newPost.setDescription(description);
    newPost.setCreatedAt(new Date());
    newPost.setUser(user);

    postRepository.save(newPost);
    return PostMapper.toPreviewPostDto(newPost);
  }

  @Override
  public String updatePost(UUID postId, UUID userId, String title, String description) {
    if (postId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: post id is required");
    }
    if (userId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: user id is required");
    }

    if ((title == null || title.isEmpty()) && (description == null || description.isEmpty())) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: title or description is required");
    }

    Post post = postRepository.findById(postId).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Invalid payload: post not found"));

    if (!post.getUser().getId().equals(userId)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: invalid post owner");
    }

    if (title != null) {
      title = title.trim();
    }

    if (description != null) {
      description = description.trim();
    }

    if (title != null && title.equals(post.getTitle()) && description != null && description.equals(post.getDescription())) {
      throw new APIException(HttpStatus.CONFLICT, "Invalid payload: same title and description");
    }

    if (title != null && !title.isEmpty() && post.getTitle().equals(title) && (description == null || description.isEmpty())) {
      throw new APIException(HttpStatus.CONFLICT, "Invalid payload: same title");
    }

    if (description != null && !description.isEmpty() && post.getDescription().equals(description) && (title == null || title.isEmpty())) {
      throw new APIException(HttpStatus.CONFLICT, "Invalid payload: same description");
    }

    if (title != null && !title.isEmpty() && !title.equals(post.getTitle())) {
      post.setTitle(title);
    }

    if (description != null && !description.isEmpty() && !description.equals(post.getDescription())) {
      post.setDescription(description);
    }

    postRepository.save(post);
    return "Post successfully updated!";
  }

  @Override
  public void deletePost(UUID postId, UUID userId) {
    if (postId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: post id is required");
    }

    if (userId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: user id is required");
    }

    Post post = postRepository.findById(postId).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Invalid payload: post not found"));

    if (!post.getUser().getId().equals(userId)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: invalid post owner");
    }

    postRepository.delete(post);
  }

  @Override
  public PostDto getPostById(UUID postId) {
    Optional<Post> postOpt = postRepository.findById(postId);
    if (postOpt.isEmpty()) {
      throw new APIException(HttpStatus.NOT_FOUND, "Post not found");
    }
    Post post = postOpt.get();
    return PostMapper.toPostDto(post);
  }

  @Override
  public List<PostDto> getBatchOfUserPosts(UUID userId, int pageNumber, int batchSize, UUID viewerId) {
    validatePagination(pageNumber, batchSize, userId);

    User viewer;
    if (viewerId != null) viewer = userRepository.findById(viewerId).orElse(null);
    else viewer = null;

    Pageable pageable = PageRequest.of(pageNumber, batchSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Post> postPage = postRepository.findByUserId(userId, pageable);
    List<Post> posts = postPage.getContent();

    return posts.stream()
            .map((post -> {
              PostDto postDto = PostMapper.toPostDto(post);
              if (viewer != null && post.getLikedBy().contains(viewer)) postDto.setLiked(true);
              return postDto;
            }))
            .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public boolean likePost(UUID postId, UUID userId) {
    Optional<Post> postOpt = postRepository.findById(postId);
    Optional<User> userOpt = userRepository.findById(userId);

    if (postOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "Post not found");
    if (userOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "User not found");

    Post post = postOpt.get();
    User user = userOpt.get();

    if (post.getLikedBy().contains(user)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "You already liked the post");
    }

    post.addLike(user);
    postRepository.save(post);
    return true;
  }

  @Transactional
  @Override
  public boolean unlikePost(UUID postId, UUID userId) {
    Optional<Post> postOpt = postRepository.findById(postId);
    Optional<User> userOpt = userRepository.findById(userId);

    if (postOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "Post not found");
    if (userOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "User not found");

    Post post = postOpt.get();
    User user = userOpt.get();
    if (!post.getLikedBy().contains(user)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "You need to like the post first");
    }

    post.removeLike(user);
    postRepository.save(post);
    return true;
  }

  @Override
  public List<PostDto> getBatchOfAllPosts(int pageNumber, int batchSize, UUID viewerId) {
    validatePagination(pageNumber, batchSize, null);

    User viewer;
    if (viewerId != null) viewer = userRepository.findById(viewerId).orElse(null);
    else viewer = null;


    Pageable pageable = PageRequest.of(pageNumber, batchSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Post> postPage = postRepository.findAll(pageable);
    List<Post> posts = postPage.getContent();

    return posts.stream()
            .map(post -> {
              PostDto postDto = PostMapper.toPreviewPostDto(post);
              if (viewer != null && post.getLikedBy().contains(viewer)) {
                postDto.setLiked(true);
              }
              return postDto;
            })
            .collect(Collectors.toList());
  }

  private void validatePagination(int pageNumber, int batchSize, UUID userId) {
    if (pageNumber < 0) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: page number must be greater than or equal to 0");
    }
    if (batchSize <= 0) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: batch size must be greater than 0");
    }
    if (userId != null && !userRepository.existsById(userId)) {
      throw new APIException(HttpStatus.NOT_FOUND, "Invalid payload: user not found");
    }
  }

}