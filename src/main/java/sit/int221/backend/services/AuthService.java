package sit.int221.backend.services;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.backend.dtos.LoginRequestDto;
import sit.int221.backend.dtos.LoginResponseDto;
import sit.int221.backend.entities.PasswordResetToken;
import sit.int221.backend.entities.User;
import sit.int221.backend.repositories.PasswordResetTokenRepository;
import sit.int221.backend.repositories.UserRepository;
import sit.int221.backend.security.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetTokenRepository passwordResetTokenRepo;


    public LoginResponseDto login(LoginRequestDto dto) {
        var user = userRepository.findByEmailIgnoreCase(dto.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You need to activate your account before signing in."
            );
        }


        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("nickname", user.getNickName());
        claims.put("email", user.getEmail());
        claims.put("role", user.getUserType());

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return LoginResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getNickName())
                .role(user.getUserType())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional(readOnly = true)
    public String refreshAccessToken(String refreshToken) {
        Claims claims = jwtUtil.extractAllClaims(refreshToken);

        var user = userRepository.findById(Integer.parseInt(claims.getSubject()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return jwtUtil.generateAccessToken(user);
    }

    @Transactional
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword)); //
        userRepository.save(user);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found"));
        emailVerificationService.createAndSendResetToken(user);
    }


    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepo.delete(resetToken);
    }


    public Integer extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);

        try {
            return Integer.parseInt(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token subject");
        }
    }
}

