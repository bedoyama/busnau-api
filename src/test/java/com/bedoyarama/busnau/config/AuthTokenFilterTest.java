package com.bedoyarama.busnau.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

  @Mock private JwtUtils jwtUtils;
  @Mock private UserDetailsService userDetailsService;
  @InjectMocks private AuthTokenFilter authTokenFilter;

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_shouldAuthenticate_whenValidBearerToken() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
    when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
    when(jwtUtils.getUserNameFromJwtToken("validToken")).thenReturn("alice");

    UserDetails ud = new User("alice", "pw", Collections.emptyList());
    when(userDetailsService.loadUserByUsername("alice")).thenReturn(ud);

    authTokenFilter.doFilterInternal(request, response, filterChain);

    verify(userDetailsService).loadUserByUsername("alice");
    verify(filterChain).doFilter(request, response);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternal_shouldNotAuthenticate_whenInvalidToken() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer bad");
    when(jwtUtils.validateJwtToken("bad")).thenReturn(false);

    authTokenFilter.doFilterInternal(request, response, filterChain);

    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternal_shouldNotAuthenticate_whenNoAuthorizationHeader() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);

    authTokenFilter.doFilterInternal(request, response, filterChain);

    verify(jwtUtils, never()).validateJwtToken(anyString());
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternal_shouldNotAuthenticate_whenHeaderNotBearer() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

    authTokenFilter.doFilterInternal(request, response, filterChain);

    verify(jwtUtils, never()).validateJwtToken(anyString());
    verify(filterChain).doFilter(request, response);
  }

  private void assertNotNull(Object obj) {
    org.junit.jupiter.api.Assertions.assertNotNull(obj);
  }

  private void assertNull(Object obj) {
    org.junit.jupiter.api.Assertions.assertNull(obj);
  }
}
