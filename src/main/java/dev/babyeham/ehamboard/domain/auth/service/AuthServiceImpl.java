package dev.babyeham.ehamboard.domain.auth.service;

import dev.babyeham.ehamboard.domain.auth.dto.AuthResponse;
import dev.babyeham.ehamboard.domain.auth.dto.SigninRequest;
import dev.babyeham.ehamboard.domain.auth.dto.SignupRequest;
import dev.babyeham.ehamboard.domain.user.entity.User;
import dev.babyeham.ehamboard.domain.user.repository.UserRepository;
import dev.babyeham.ehamboard.global.exception.DuplicateUsernameException;
import dev.babyeham.ehamboard.global.exception.InvalidCredentialsException;
import dev.babyeham.ehamboard.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException("이미 존재하는 사용자 이름입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .userId(savedUser.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse signin(SigninRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(user);

            return AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .userId(user.getId())
                    .build();
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }
}
