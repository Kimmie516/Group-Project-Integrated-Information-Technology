package sit.int221.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.backend.dtos.profile.*;
import sit.int221.backend.entities.SellerProfile;
import sit.int221.backend.entities.User;
import sit.int221.backend.repositories.SellerProfileRepository;
import sit.int221.backend.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepo;
    private final SellerProfileRepository sellerRepo;

    public ProfileView getProfile(Integer id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if ("BUYER".equalsIgnoreCase(user.getUserType())) {
            return new BuyerProfileDto(
                    user.getId(),
                    user.getUserType(),
                    user.getNickName(),
                    user.getEmail(),
                    user.getFullName()
            );
        } else {
            SellerProfile seller = sellerRepo.findByUser(user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller profile not found"));

            return new SellerProfileDto(
                    user.getId(),
                    user.getUserType(),
                    user.getNickName(),
                    user.getEmail(),
                    user.getFullName(),
                    maskPhone(user.getPhoneNumber()),
                    maskBankAccount(seller.getBankAccount()),
                    seller.getBankName(),
                    seller.getBankAccount() != null
            );
        }
    }

    public ProfileView updateProfile(Integer id, ProfileUpdateDto dto, Integer currentUserId) {
        if (!id.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own profile");
        }

        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setNickName(dto.getNickName());
        user.setFullName(dto.getFullName());
        userRepo.save(user);

        return getProfile(id);
    }


    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        int len = phone.length();
        return "xxxxx"
                + phone.charAt(len - 4)
                + phone.charAt(len - 3)
                + phone.charAt(len - 2)
                + "x";
    }

    private String maskBankAccount(String bankAccount) {
        if (bankAccount == null || bankAccount.length() < 4) return bankAccount;
        int len = bankAccount.length();
        return "xxxxx"
                + bankAccount.charAt(len - 4)
                + bankAccount.charAt(len - 3)
                + bankAccount.charAt(len - 2)
                + "x";
    }

}
