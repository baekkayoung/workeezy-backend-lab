package com.together.workeezy.user.service;

import com.together.workeezy.common.exception.CustomException;
import com.together.workeezy.user.dto.UserPasswordUpdateRequest;
import com.together.workeezy.user.entity.User;
import com.together.workeezy.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.together.workeezy.common.exception.ErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void updatePassword_duplicatePassword() {
        // given
        User user = new User("test@test.com", "encoded");

        given(userRepository.findByEmail(user.getEmail()))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("Password1@@", "encoded"))
                .willReturn(true);

        UserPasswordUpdateRequest request =
                new UserPasswordUpdateRequest(
                        "Password1@@",
                        "Password1@@",
                        "Password1@@"
                );

        // when & then
        assertThatThrownBy(() ->
                userService.updatePassword(user.getEmail(), request)
        ).isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", DUPLICATE_PASSWORD);
    }
}
