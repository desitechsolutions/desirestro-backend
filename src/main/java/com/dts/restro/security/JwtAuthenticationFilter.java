package com.dts.restro.security;

import com.dts.restro.common.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that:
 * <ol>
 *   <li>Extracts and validates the Bearer token from the Authorization header.</li>
 *   <li>Sets the Spring Security {@code Authentication} from the token claims.</li>
 *   <li>Populates {@link TenantContext} with the {@code restaurantId} claim so
 *       the Hibernate restaurant filter is automatically applied to all
 *       repository calls in the same request thread.</li>
 *   <li>Always clears {@link TenantContext} in a {@code finally} block to
 *       prevent thread-local leaks in pooled threads.</li>
 * </ol>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                String username = jwtUtil.extractUsername(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        // Set Spring Security context
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        // Populate TenantContext from restaurantId JWT claim
                        Long restaurantId = jwtUtil.extractRestaurantId(jwt);
                        if (restaurantId != null) {
                            TenantContext.setCurrentRestaurantId(restaurantId);
                            log.debug("TenantContext set: restaurantId={} user={}", restaurantId, username);
                        }
                    }
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.warn("JWT filter error for {}: {}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication failed\"}");
        } finally {
            // Always clear TenantContext to prevent thread-local leaks
            TenantContext.clear();
        }
    }
}
