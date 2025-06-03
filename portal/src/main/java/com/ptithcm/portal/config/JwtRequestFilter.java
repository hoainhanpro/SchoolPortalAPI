package com.ptithcm.portal.config;

import com.ptithcm.portal.service.UserDetailsServiceImpl;
import com.ptithcm.portal.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays; // Add this import
import java.util.List;   // Add this import

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private JWTUtil jwtUtil;

    // Define public path prefixes based on SecurityConfig
    // Updated to be more specific: only /api/auth/login is a public auth path.
    private static final List<String> PUBLIC_PATHS = Arrays.asList("/api/auth/login", "/api/schedule/");

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Check if the request path exactly matches /api/auth/login or starts with /api/schedule/
        boolean isPublicPath = PUBLIC_PATHS.stream().anyMatch(publicPath -> {
            if (publicPath.endsWith("/")) { // If it's a prefix like /api/schedule/
                return requestPath.startsWith(request.getContextPath() + publicPath);
            } else { // If it's an exact path like /api/auth/login
                return requestPath.equals(request.getContextPath() + publicPath);
            }
        });
        
        // If contextPath is not explicitly set or is root ("/"), request.getContextPath() will be empty.
        // If you are sure about no context path or it's always root, you can simplify to:
        // boolean isPublicPath = PUBLIC_PATH_PREFIXES.stream().anyMatch(requestPath::startsWith);


        if (isPublicPath) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        // System.out.println("Authorization Header: " + requestTokenHeader);
        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            // System.out.println("JWT Token: " + jwtToken);
            try {
                username = jwtUtil.getSubject(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.warn("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token has expired");
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String or is null");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsServiceImpl.loadUserByUsername(username);

            if (jwtUtil.isTokenValid(jwtToken, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}
