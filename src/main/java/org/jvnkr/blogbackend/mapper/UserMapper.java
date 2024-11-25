package org.jvnkr.blogbackend.mapper;

import org.jvnkr.blogbackend.dto.UserResponseDto;
import org.jvnkr.blogbackend.entity.User;

public class UserMapper {
  public static UserResponseDto toUserResponseDto(User user) {
    return new UserResponseDto(
            user.getUsername(),
            user.getName(),
            user.getEmail()
    );
  }
}
