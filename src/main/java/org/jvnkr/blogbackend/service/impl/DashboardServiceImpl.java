package org.jvnkr.blogbackend.service.impl;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.DashboardChartDto;
import org.jvnkr.blogbackend.dto.DashboardDto;
import org.jvnkr.blogbackend.dto.PostDto;
import org.jvnkr.blogbackend.dto.TopUserDto;
import org.jvnkr.blogbackend.entity.Post;
import org.jvnkr.blogbackend.entity.User;
import org.jvnkr.blogbackend.exception.APIException;
import org.jvnkr.blogbackend.mapper.PostMapper;
import org.jvnkr.blogbackend.mapper.UserMapper;
import org.jvnkr.blogbackend.repository.PostRepository;
import org.jvnkr.blogbackend.repository.ReportRepository;
import org.jvnkr.blogbackend.repository.UserRepository;
import org.jvnkr.blogbackend.service.DashboardService;
import org.jvnkr.blogbackend.service.Roles;
import org.jvnkr.blogbackend.utils.Pagination;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DashboardServiceImpl implements DashboardService {
  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final ReportRepository reportRepository;

  @Override
  public DashboardDto getDashboard(int year, UUID viewerId) {
    if (viewerId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Viewer ID must not be null");
    }
    User viewer = userRepository.findById(viewerId).orElse(null);
    if (viewer == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Viewer not found");
    }
    boolean hasAdminRole = viewer.getRoles()
            .stream()
            .map(role -> Roles.fromName(role.getName()))
            .anyMatch(role -> role == Roles.ROLE_ADMIN);

    if (!hasAdminRole) {
      throw new APIException(HttpStatus.UNAUTHORIZED, "Access denied");
    }

    int postsCount = (int) postRepository.count();
    int usersCount = (int) userRepository.count();
    int reportsCount = (int) reportRepository.count();

    List<User> users = postRepository.findTopUsersByPostsAndEngagement(PageRequest.of(0, 5));

    List<TopUserDto> topUsers = users.stream().map(UserMapper::toTopUserDto).toList();

    LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0);
    LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);
    List<DashboardChartDto> monthlyTopPosts = postRepository.findPostCountsForCurrentYear(startOfYear, endOfYear);

    int currentYear = LocalDate.now().getYear();
    int earliestYear = postRepository.findYearOfOldestPost().orElse(currentYear);

    return new DashboardDto(
            postsCount,
            usersCount,
            reportsCount,
            earliestYear,
            topUsers,
            monthlyTopPosts
    );
  }

  @Override
  public List<PostDto> getBatchOfDashoardPosts(int pageNumber, int batchSize) {
    Pagination.validate(pageNumber, batchSize, null, userRepository);

    Pageable pageable = PageRequest.of(pageNumber, batchSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    List<Post> posts = postRepository.findTopPerformingPosts(pageable);

    return posts.stream()
            .map(post -> PostMapper.toPreviewPostDto(post, null))
            .toList();
  }
}
