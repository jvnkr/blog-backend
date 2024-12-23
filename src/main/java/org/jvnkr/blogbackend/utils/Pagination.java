package org.jvnkr.blogbackend.utils;

import org.jvnkr.blogbackend.exception.APIException;
import org.jvnkr.blogbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class Pagination {
  public static void validate(int pageNumber, int batchSize, UUID userId, UserRepository userRepository) {
    if (userRepository == null) {
      throw new IllegalStateException("UserRepository has not been initialized");
    }
    if (pageNumber < 0) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Page number must be non-negative.");
    }
    if (batchSize <= 0) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Batch size must be positive.");
    }
    if (userId != null && !userRepository.existsById(userId)) {
      throw new APIException(HttpStatus.NOT_FOUND, "User not found.");
    }
  }
}