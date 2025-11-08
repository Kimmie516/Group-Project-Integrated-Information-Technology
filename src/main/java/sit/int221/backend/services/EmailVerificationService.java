package sit.int221.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.backend.entities.EmailVerificationToken;
import sit.int221.backend.entities.User;
import sit.int221.backend.repositories.EmailVerificationTokenRepository;
import sit.int221.backend.repositories.UserRepository;
import sit.int221.backend.entities.PasswordResetToken;
import sit.int221.backend.repositories.PasswordResetTokenRepository;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;
    private final PasswordResetTokenRepository passwordResetTokenRepo;


    @Value("${app.verify.base-url:http://localhost:5173/verify-email}")
    private String baseUrl;

    @Value("${app.verify.ttl-hours:24}")
    private long ttlHours;

    @Value("${app.reset.base-url:http://localhost:5173/reset-password}")
    private String resetBaseUrl;

    @Value("${app.reset.ttl-hours:1}")
    private long resetTtlHours;

    @Async
    @Transactional
    public void createAndSendToken(User user) {
        try {
            tokenRepo.deleteByUser_IdAndUsedAtIsNull(user.getId());

            String tok;
            do {
                tok = java.util.UUID.randomUUID().toString().replace("-", "");
            } while (tokenRepo.existsByToken(tok));

            var t = new EmailVerificationToken();
            t.setUser(user);
            t.setToken(tok);
            t.setCreatedAt(LocalDateTime.now());
            t.setExpiresAt(LocalDateTime.now().plusHours(ttlHours));
            tokenRepo.save(t);

            String link = buildVerifyLink(tok);
            String subject = "Verify your ITB-MShop account";
            String body = """
                    Hello %s,

                    Please confirm your email address by clicking the link below:
                    %s

                    This link will expire in %d hour%s.

                    If you didn’t request this, you can safely ignore this email.

                    Thanks,
                    ITB-MShop Team
                    """.formatted(
                    safe(user.getFullName(), user.getEmail()),
                    link,
                    ttlHours,
                    (ttlHours == 1 ? "" : "s")
            );

            sendMail(user.getEmail(), subject, body);

            System.out.println("[ASYNC EMAIL] Verification mail scheduled for " + user.getEmail());

        } catch (Exception e) {
            System.err.println("[ASYNC EMAIL ERROR] Failed to send verification mail for "
                    + user.getEmail() + " → " + e.getMessage());
        }
    }

    @Transactional
    public void verify(String tokenStr) {
        var t = tokenRepo.findByToken(tokenStr)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));

        if (t.getUsedAt() != null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Token already used");
        }
        if (t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Token expired");
        }

        User user = t.getUser();
        user.setIsActive(true);
        user.setUpdatedOn(LocalDateTime.now());
        userRepo.save(user);

        t.setUsedAt(LocalDateTime.now());
        tokenRepo.save(t);
    }

    @Async
    @Transactional
    public void resend(String email) {
        var user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already active");
        }

        createAndSendToken(user);
    }

    private String buildVerifyLink(String token) {
        String sep = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + sep + "token=" + token;
    }

    private void sendMail(String to, String subject, String body) {
        try {
            var msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            System.out.println("[MAIL MOCK] to=" + to + " | subject=" + subject + "\n" + body);
        }
    }

    private String safe(String name, String fallback) {
        if (name == null || name.isBlank()) return fallback;
        return name;
    }

    @Async
    @Transactional
    public void createAndSendResetToken(User user) {
        passwordResetTokenRepo.deleteAll(
                passwordResetTokenRepo.findAll()
                        .stream().filter(t -> t.getUser().getId().equals(user.getId())).toList()
        );

        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1)); // 1 ชม.
        passwordResetTokenRepo.save(resetToken);

        String link = buildResetLink(token);


        String subject = "Reset your ITB-MShop password";
        String body = """
            Hi %s,

            We received a request to reset your ITB-MShop password.
            Click the link below to set a new password:
            %s

            This link will expire in 1 hour.

            If you didn’t request this, you can ignore this email.

            Thanks,
            ITB-MShop Team
            """.formatted(safe(user.getFullName(), user.getEmail()), link);

        sendMail(user.getEmail(), subject, body);
    }

    @Transactional
    public void verifyAndResetPassword(String token, String newPassword, PasswordEncoder passwordEncoder) {
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Reset token expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        passwordResetTokenRepo.delete(resetToken);
    }
    private String buildResetLink(String token) {
        String sep = resetBaseUrl.contains("?") ? "&" : "?";
        return resetBaseUrl + sep + "token=" + token;
    }
}
