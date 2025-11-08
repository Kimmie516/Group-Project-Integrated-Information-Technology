package sit.int221.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.backend.services.EmailVerificationService;

import java.util.Map;

@RestController
@RequestMapping("/v2/auth")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping(
            value = "/verify-email",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
        emailVerificationService.verify(req.getToken());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Your account has been successfully activated."));
    }

    @PostMapping(
            value = "/resend-verification",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> resend(@Valid @RequestBody ResendVerificationRequest req) {
        emailVerificationService.resend(req.getEmail());
        return ResponseEntity.ok(Map.of("message", "A new verification email has been sent."));
    }


    @Data
    public static class VerifyEmailRequest {
        @NotBlank
        private String token;
    }

    @Data
    public static class ResendVerificationRequest {
        @NotBlank
        @Email
        @Size(max = 100)
        private String email;
    }
}
