package sit.int221.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.int221.backend.repositories.UserRepository;
import sit.int221.backend.services.TokenBlacklistService;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;
    private final TokenBlacklistService tokenBlacklistService;


    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepo, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/v2/auth/login") ||
                path.startsWith("/v2/auth/refresh") ||
                path.startsWith("/v2/users/register") ||
                path.startsWith("/v2/auth/verify-email") ||
                path.startsWith("/v2/auth/resend-verification")) {
            System.out.println(">> JwtAuthFilter skip path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            System.out.println(">> JwtAuthFilter: Missing Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);


        if (tokenBlacklistService.contains(token)) {
            System.out.println(">> JwtAuthFilter: Token is blacklisted");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            var user = userRepo.findById(Integer.parseInt(claims.getSubject()))
                    .orElse(null);

            if (user != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user, null, null
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println(">> JwtAuthFilter: Authenticated user=" + user.getEmail());
            }

        } catch (JwtException e) {
            System.out.println(">> JwtAuthFilter: Invalid token - " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
