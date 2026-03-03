package com.bedoyarama.busnau.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

class RateLimitConfigTest {

  private final RateLimitConfig config = new RateLimitConfig();

  @Test
  void rateLimitFilter_shouldAllowRequestsWithinLimit() throws Exception {
    OncePerRequestFilter filter = config.rateLimitFilter();
    FilterChain chain = mock(FilterChain.class);

    for (int i = 0; i < 10; i++) {
      MockHttpServletRequest req = new MockHttpServletRequest();
      req.setRemoteAddr("10.0.0.1");
      filter.doFilter(req, new MockHttpServletResponse(), chain);
    }

    verify(chain, times(10)).doFilter(any(), any(HttpServletResponse.class));
  }

  @Test
  void rateLimitFilter_shouldBlock11thRequest() throws Exception {
    OncePerRequestFilter filter = config.rateLimitFilter();
    FilterChain chain = mock(FilterChain.class);

    // exhaust the 10-token bucket
    for (int i = 0; i < 10; i++) {
      MockHttpServletRequest req = new MockHttpServletRequest();
      req.setRemoteAddr("10.0.0.2");
      filter.doFilter(req, new MockHttpServletResponse(), chain);
    }

    // 11th request should be rejected
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRemoteAddr("10.0.0.2");
    MockHttpServletResponse blocked = new MockHttpServletResponse();
    filter.doFilter(req, blocked, chain);

    assertEquals(429, blocked.getStatus());
    assertEquals("Too many requests", blocked.getContentAsString());
  }

  @Test
  void rateLimitFilter_shouldUseXForwardedForIfPresent() throws Exception {
    OncePerRequestFilter filter = config.rateLimitFilter();
    FilterChain chain = mock(FilterChain.class);

    // exhaust bucket for forwarded IP
    for (int i = 0; i < 10; i++) {
      MockHttpServletRequest req = new MockHttpServletRequest();
      req.setRemoteAddr("127.0.0.1");
      req.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");
      filter.doFilter(req, new MockHttpServletResponse(), chain);
    }

    // next request from same forwarded IP should be blocked
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRemoteAddr("127.0.0.1");
    req.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");
    MockHttpServletResponse blocked = new MockHttpServletResponse();
    filter.doFilter(req, blocked, chain);

    assertEquals(429, blocked.getStatus());
  }
}
