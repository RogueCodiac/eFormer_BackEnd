package eformer.back.eformer_backend.utility.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jManager;

    public JwtAuthenticationFilter(JwtService jManager) {
        this.jManager = jManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        /* Contains the token */
        final String authHeader = request.getHeader("Authorization");

        /* A valid token must start with `Bearer` */
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            /* Invalid token */
            filterChain.doFilter(request, response);
            return;
        }

        /* Skip `Bearer` */
        final String jwt = authHeader.substring(7);

        final String username = jManager.extractUsername(jwt);

    }
}
