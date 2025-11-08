package sit.int221.backend.dtos;


import jakarta.validation.constraints.*;

public record LoginRequestDto(
        @NotBlank @Email @Size(max = 50) String email,
        @NotNull  @Size(min = 1, max = 14) String password
) {}
