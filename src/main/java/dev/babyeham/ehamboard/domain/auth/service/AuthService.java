package dev.babyeham.ehamboard.domain.auth.service;

import dev.babyeham.ehamboard.domain.auth.dto.AuthResponse;
import dev.babyeham.ehamboard.domain.auth.dto.SigninRequest;
import dev.babyeham.ehamboard.domain.auth.dto.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);
    AuthResponse signin(SigninRequest request);
}
