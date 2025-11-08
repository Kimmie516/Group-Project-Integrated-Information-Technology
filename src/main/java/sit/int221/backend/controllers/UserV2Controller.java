package sit.int221.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.backend.dtos.UserRegisterDto;
import sit.int221.backend.dtos.UserResponseDto;
import sit.int221.backend.dtos.profile.ProfileUpdateDto;
import sit.int221.backend.services.UserService;
import sit.int221.backend.services.ProfileService;

import java.util.Map;

@RestController
@RequestMapping("/v2/users")
@RequiredArgsConstructor
public class UserV2Controller {

    private final UserService userService;
    private final ProfileService profileService;

    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDto> registerJson(@Valid @RequestBody UserRegisterDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(dto));
    }

    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDto> registerMultipart(@Valid @ModelAttribute UserRegisterDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Integer id) {
        return ResponseEntity.ok(profileService.getProfile(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Integer id,
            @Valid @RequestBody ProfileUpdateDto dto,
            @AuthenticationPrincipal(expression = "id") Integer currentUserId
    ) {
        return ResponseEntity.ok(profileService.updateProfile(id, dto, currentUserId));
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal(expression = "id") Integer currentUserId) {

        if (!id.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only change your own password");
        }

        userService.changePassword(id, body.get("oldPassword"), body.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

}
