package sit.int221.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.backend.entities.EmailVerificationToken;

import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUser_IdAndUsedAtIsNull(Integer userId);
    boolean existsByToken(String token);
}

