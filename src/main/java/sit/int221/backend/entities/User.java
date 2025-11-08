package sit.int221.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "userType", nullable = false, length = 20)
    private String userType;

    @Size(max = 50)
    @NotNull
    @Column(name = "nickName", nullable = false, length = 50)
    private String nickName;

    @Size(max = 100)
    @NotNull
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Size(max = 100)
    @NotNull
    @Column(name = "fullName", nullable = false, length = 100)
    private String fullName;

    @Size(max = 20)
    @Column(name = "phoneNumber", length = 20)
    private String phoneNumber;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "isActive", nullable = false)
    private Boolean isActive = false;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "createdOn")
    private LocalDateTime createdOn;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updatedOn")
    private LocalDateTime updatedOn;

}