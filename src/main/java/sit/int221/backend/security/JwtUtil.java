package sit.int221.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sit.int221.backend.entities.User;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;

    public JwtUtil(@Value("${app.jwt.secret:default-very-secret-key-32chars!!}") String secret) {
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    private static final long ACCESS_TOKEN_EXP = 30 * 60 * 1000;
    private static final long REFRESH_TOKEN_EXP = 24 * 60 * 60 * 1000;

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setIssuer("http://intproj24.sit.kmutt.ac.th/itb-mshop")
                .setSubject(user.getId().toString())
                .claim("nickName", user.getNickName())
                .claim("email", user.getEmail())
                .claim("role", user.getUserType())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXP))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setIssuer("http://intproj24.sit.kmutt.ac.th/itb-mshop")
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
