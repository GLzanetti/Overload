package br.com.overload.gatewayservice.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@FilterRegistration
public class JwtAuthFilter extends OncePerRequestFilter {

    private final RotasPublicasProperties rotasPublicasProperties;
    private final JwtService jwtService;

    public JwtAuthFilter(RotasPublicasProperties rotasPublicasProperties, JwtService jwtService) {
        this.rotasPublicasProperties = rotasPublicasProperties;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (rotasPublicasProperties.getRotasPublicas().contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);

        String userId;
        try {
            userId = jwtService.validarToken(token);
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        HttpServletRequest requestComUserId = new RequestComUserId(request, userId);
        filterChain.doFilter(requestComUserId, response);
    }
}
