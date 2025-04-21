package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.service.dto.request.SigninRequest;
import org.example.expert.domain.auth.service.dto.request.SignupRequest;
import org.example.expert.domain.auth.service.dto.response.SigninResponse;
import org.example.expert.domain.auth.service.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.service.AuthService;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest("test@test.com", "password", "USER");
        User savedUser = new User(request.getEmail(), "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(savedUser, "id", 1L);
        String bearerToken = "bearerToken";

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any())).willReturn(savedUser);
        given(jwtUtil.createToken(1L, "test@test.com", UserRole.USER)).willReturn(bearerToken);

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertEquals(response.getBearerToken(), bearerToken);
    }

    @Test
    @DisplayName("로그인 성공")
    void signin_success() {
        // given
        SigninRequest request = new SigninRequest("test@test.com", "password");
        User user = new User(request.getEmail(), "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        String bearerToken = "bearerToken";

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(1L, "test@test.com", UserRole.USER)).willReturn(bearerToken);

        // when
        SigninResponse response = authService.signin(request);

        // then
        assertEquals(response.getBearerToken(), bearerToken);
    }
}
