package org.jvnkr.blogbackend.config;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.exception.CustomAccessDeniedHandler;
import org.jvnkr.blogbackend.security.JwtAuthenticationEntryPoint;
import org.jvnkr.blogbackend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@AllArgsConstructor
public class SpringSecurityConfig {
  // private UserDetailsService userDetailsService;
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  // @Autowired
  private CustomAccessDeniedHandler customAccessDeniedHandler;

  // Handles extraction and validation of JWTs
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public static PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests((authorize) -> {
              authorize.requestMatchers(HttpMethod.POST, "/api/v1/posts/batch").permitAll();
              authorize.requestMatchers(HttpMethod.GET, "/api/v1/email/verify").permitAll();
              authorize.requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll();
              authorize.requestMatchers(HttpMethod.GET, "/api/auth/**").permitAll();
              authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
              authorize.anyRequest().authenticated();
            });

    // Allows Basic Auth (username:password)
    // http.httpBasic(Customizer.withDefaults());

    http.exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(customAccessDeniedHandler));

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
          throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000"
    ));

    configuration.setAllowedOriginPatterns(List.of(
            "http://192.168.0.*:3000",
            "http://192.168.1.*:3000",
            "http://192.168.100.*:3000",
            "http://10.0.0.*:3000"
    ));

    // Specify allowed methods
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    // Specify allowed headers
    configuration.setAllowedHeaders(List.of("*"));

    // Allow credentials
    configuration.setAllowCredentials(true);

    // Register the configuration for all paths
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
  // In-Memory Authentication
  // @Bean
  // public UserDetailsService userDetailsService() {
  // UserDetails jovan=
  // User.builder().username("jovan").password(passwordEncoder().encode("password")).roles("USER").build();
  // UserDetails admin=
  // User.builder().username("admin").password(passwordEncoder().encode("admin")).roles("ADMIN").build();
  //
  // return new InMemoryUserDetailsManager(jovan, admin);
  // }
}
