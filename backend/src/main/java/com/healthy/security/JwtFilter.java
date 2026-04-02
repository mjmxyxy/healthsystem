package com.healthy.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.healthy.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JwtFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, jakarta.servlet.ServletException {
        String h = request.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            try {
                DecodedJWT jwt = JwtUtil.verify(token);
                String role = jwt.getClaim("role").asString();
                Long userId = jwt.getClaim("userId").asLong();
                String principal = userId == null ? jwt.getSubject() : String.valueOf(userId);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (role == null ? "USER" : role.toUpperCase()))));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ex) {
                // invalid token -> ignore, will be unauthenticated
            }
        }
        filterChain.doFilter(request, response);
    }
}
