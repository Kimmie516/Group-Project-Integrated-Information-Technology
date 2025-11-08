package sit.int221.backend.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.backend.dtos.LoginRequestDto;
import sit.int221.backend.dtos.LoginResponseDto;
import sit.int221.backend.services.AuthService;
import sit.int221.backend.services.TokenBlacklistService;

import java.util.Map;

@RestController
@RequestMapping("/v2/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequestDto dto,
            HttpServletResponse response
    ) {
        LoginResponseDto result = authService.login(dto);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", result.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, Object> body = Map.of("access_token", result.getAccessToken());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "BAD_REQUEST",
                    "message", "No refresh token"
            ));
        }

        try {
            String newAccessToken = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(Map.of("access_token", newAccessToken));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", e.getStatusCode().toString(),
                    "message", e.getReason()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "INVALID_TOKEN",
                    "message", "Refresh token is invalid or expired"
            ));
        }
    }

    private final TokenBlacklistService tokenBlacklistService;
    @PostMapping("/signout")
    public ResponseEntity<Void> signout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletResponse response) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.add(token);
        }

        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/change-password/{id}")
    public ResponseEntity<?> changePassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader
    ) {
        Integer currentUserId = authService.extractUserId(authHeader);

        if (!id.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only change your own password");
        }

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        authService.changePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(Map.of("message", "Reset link sent if email is registered"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }


}
