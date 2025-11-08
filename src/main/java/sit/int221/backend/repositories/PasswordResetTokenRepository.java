package sit.int221.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.backend.entities.PasswordResetToken;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByToken(String token);
}
