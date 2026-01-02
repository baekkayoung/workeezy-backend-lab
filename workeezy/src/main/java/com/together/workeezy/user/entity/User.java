package com.together.workeezy.user.entity;

import com.together.workeezy.common.exception.CustomException;
import com.together.workeezy.reservation.domain.Reservation;
import com.together.workeezy.reservation.domain.ReservationModify;
import com.together.workeezy.user.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.together.workeezy.common.exception.ErrorCode.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
//@Builder
@Table(name = "tb_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(nullable = false, length = 100)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "user_pwd", nullable = false)
    private String password;

    @Size(max = 100)
    @NotNull
    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    @NotNull
    @Column(nullable = false)
    private LocalDate birth;

    @Size(max = 30)
    @NotNull
    @Column(nullable = false, length = 30)
    private String company;

    @NotNull
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role; // USER, ADMIN

    @OneToMany
    @JoinColumn(name = "user_id")
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "user_id")
    private List<ReservationModify> reservationModifys = new ArrayList<>();

    public static User reference(Long id) {
        User user = new User();
        user.id = id;
        return user;
    }

    // ======== 도메인 동작 ========
    public void changePhone(String newPhone) {
//        validatePhone(newPhone);
        this.phone = newPhone;
    }

    public void changePassword(String rawPassword, PasswordEncoder encoder) {
        validatePasswordRule(rawPassword);
        this.password = encoder.encode(rawPassword);
    }

    // ======== 검증 로직 =========
//    private static final Pattern PHONE_REGEX =
//            Pattern.compile("^010-\\d{4}-\\d{4}$");
//
//    private void validatePhone(String phone) {
//        if (phone == null || phone.isBlank()) {
//            throw new IllegalArgumentException("Phone number required");
//        }
//
//        if (!PHONE_REGEX.matcher(phone).matches()) {
//            throw new IllegalArgumentException("Invalid phone format. (010-XXXX-XXXX)");
//        }
//    }

    private void validatePasswordRule(String password) {
//        if (password == null || password.isBlank()) {
//            throw new IllegalArgumentException("비밀번호가 비어있습니다.");
//        }

        if (password.length() < 8 || password.length() > 16) {
            throw new CustomException(INVALID_PASSWORD_LENGTH);
        }

        if (!password.matches(".*[0-9].*")) {
            throw new CustomException(INVALID_PASSWORD_NUMBER);
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new CustomException(INVALID_PASSWORD_UPPER);
        }

        if (!password.matches(".*[a-z].*")) {
            throw new CustomException(INVALID_PASSWORD_LOWER);
        }

        if (!password.matches(".*[!@#$%^&*].*")) {
            throw new CustomException(INVALID_PASSWORD_SPECIAL);
        }
    }
}