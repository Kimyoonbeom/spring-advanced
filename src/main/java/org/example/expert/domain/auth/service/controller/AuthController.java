package org.example.expert.domain.auth.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.auth.service.dto.request.SigninRequest;
import org.example.expert.domain.auth.service.dto.request.SignupRequest;
import org.example.expert.domain.auth.service.dto.response.SigninResponse;
import org.example.expert.domain.auth.service.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/signup")
    public SignupResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
        return authService.signup(signupRequest);
    }

    @PostMapping("/auth/signin")
    public SigninResponse signin(@Valid @RequestBody SigninRequest signinRequest) {
        return authService.signin(signinRequest);
    }
}
