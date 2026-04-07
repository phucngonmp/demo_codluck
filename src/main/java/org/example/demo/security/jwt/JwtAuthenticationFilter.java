package org.example.demo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final org.example.demo.security.jwt.CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            try {
                // Strict parse: signature + iss + aud + exp (+ skew) inside validateAndGetClaims
                Claims claims = jwtUtil.validateAndGetClaims(token);
                String username = claims.getSubject();

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Load user (keeps account status checks: enabled/locked/etc.)
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Defense-in-depth: ensure token subject matches the current user
                    if (!username.equals(userDetails.getUsername())) {
                        throw new io.jsonwebtoken.JwtException("Token subject mismatch");
                    }

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (ExpiredJwtException ex) {
                logger.warn("JWT token expired: {}", ex.getMessage());
                // Set a custom header to indicate token expiration
                response.setHeader("X-Token-Expired", "true");
                SecurityContextHolder.clearContext();
            } catch (JwtException ex) {
                logger.error("JWT validation failed: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            } catch (Exception ex) {
                logger.error("Unexpected error during JWT validation: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/login")
                || path.startsWith("/auth/signup")
                || path.startsWith("/auth/new-access-token")
                || path.startsWith("/auth/login-status");
    }
}


