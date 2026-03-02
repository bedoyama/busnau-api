package com.bedoyarama.busnau.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class RateLimitConfig {

  private static final Logger logger = LoggerFactory.getLogger(RateLimitConfig.class);
  private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

  @Bean(name = "rateLimitFilter")
  public OncePerRequestFilter rateLimitFilter() {
    return new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(
          HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {

        String ip = getClientIP(request);
        Bucket bucket = cache.computeIfAbsent(ip, this::newBucket);

        if (bucket.tryConsume(1)) {
          filterChain.doFilter(request, response);
        } else {
          logger.warn("Rate limit exceeded for IP: " + ip);
          response.setStatus(429);
          response.getWriter().write("Too many requests");
        }
      }

      private Bucket newBucket(String ip) {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
            .build();
      }

      private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
          return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
      }
    };
  }
}
