package org.jvnkr.blogbackend.security;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.entity.User;
import org.jvnkr.blogbackend.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public CustomUserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not exists by Username or Email"));

    Set<GrantedAuthority> authorities = user.getRoles().stream()
            .map((role) -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toSet());

    return new CustomUserDetails(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            authorities
    );
  }

  public UserDetails loadUserById(UUID userId) throws UsernameNotFoundException {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    Set<GrantedAuthority> authorities = user.getRoles().stream()
            .map((role) -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toSet());

    return new CustomUserDetails(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            authorities
    );
  }

  public User loadUserEntityById(UUID userId) {
    return userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found by ID"));
  }
}