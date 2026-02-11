package org.iwouldargue.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws IOException, ServletException {

        String discussionId =
                request.getHeader("X-Session-Discussion-Id");

        if (discussionId != null) {
            Session session = new Session(
                    request.getHeader("X-Session-Display-Name"),
                    UUID.fromString(discussionId),
                    Role.valueOf(request.getHeader("X-Session-Role")),
                    UUID.fromString(request.getHeader("X-Session-User-Id"))
            );

            Authentication auth =
                    new UsernamePasswordAuthenticationToken(
                            session,
                            null,
                            List.of()
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
