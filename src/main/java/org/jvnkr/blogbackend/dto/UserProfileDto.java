package org.jvnkr.blogbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
  private String name;
  private String username;
  private boolean verified;
  private String bio;
  private int followers;
  private int following;
  private Date createdAt;
  private int postsCount;
  private boolean isFollowingUser;
}
