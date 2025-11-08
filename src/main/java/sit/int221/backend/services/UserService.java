package sit.int221.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.backend.dtos.UserRegisterDto;
import sit.int221.backend.dtos.UserResponseDto;
import sit.int221.backend.entities.SellerProfile;
import sit.int221.backend.entities.User;
import sit.int221.backend.repositories.SellerProfileRepository;
import sit.int221.backend.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public UserResponseDto registerUser(UserRegisterDto dto) {
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        String type = Optional.ofNullable(dto.getUserType()).orElse("BUYER").toUpperCase();
        if (!type.equals("BUYER") && !type.equals("SELLER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User type must be BUYER or SELLER");
        }

        User user = new User();
        user.setNickName(dto.getNickName());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setUserType(type);
        user.setPhoneNumber("SELLER".equals(type) ? dto.getPhoneNumber() : null);
        user.setIsActive(false);
        user.setCreatedOn(LocalDateTime.now());
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);

        if ("SELLER".equals(type)) {
            if (dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required for seller");
            if (dto.getBankAccount() == null || dto.getBankAccount().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bank account is required for seller");
            if (dto.getBankName() == null || dto.getBankName().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bank name is required for seller");
            if (dto.getIdCardNumber() == null || dto.getIdCardNumber().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID card number is required for seller");
            if (dto.getIdCardImageFront() == null || dto.getIdCardImageFront().isEmpty()
                    || dto.getIdCardImageBack() == null || dto.getIdCardImageBack().isEmpty())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seller must upload both ID card images");

            SellerProfile sp = new SellerProfile();
            sp.setUser(user);
            sp.setBankAccount(dto.getBankAccount());
            sp.setBankName(dto.getBankName());
            sp.setIdCardNumber(dto.getIdCardNumber());
            sp.setIdCardImageFront("uploads/" + dto.getIdCardImageFront().getOriginalFilename());
            sp.setIdCardImageBack("uploads/" + dto.getIdCardImageBack().getOriginalFilename());
            sellerProfileRepository.save(sp);
        }

        try {
            emailVerificationService.createAndSendToken(user);
        } catch (Exception e) {
            System.out.println("[EMAIL] send verification failed for " + user.getEmail() + " : " + e.getMessage());
        }

        UserResponseDto res = new UserResponseDto();
        res.setId(user.getId());
        res.setNickName(user.getNickName());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setIsActive(user.getIsActive());
        res.setUserType(user.getUserType());
        return res;
    }

    @Transactional
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&/_])[A-Za-z\\d@$!%*?&/_]{8,}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must contain uppercase, lowercase, number and special character");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
