package sit.int221.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "seller_profile")
public class SellerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 50)
    @NotNull
    @Column(name = "bankAccount", nullable = false, length = 50)
    private String bankAccount;

    @Size(max = 100)
    @NotNull
    @Column(name = "bankName", nullable = false, length = 100)
    private String bankName;

    @Size(max = 50)
    @NotNull
    @Column(name = "idCardNumber", nullable = false, length = 50)
    private String idCardNumber;

    @Size(max = 255)
    @NotNull
    @Column(name = "idCardImageFront", nullable = false)
    private String idCardImageFront;

    @Size(max = 255)
    @NotNull
    @Column(name = "idCardImageBack", nullable = false)
    private String idCardImageBack;

}