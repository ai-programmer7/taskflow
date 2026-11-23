
package com.taskflow.service;

import com.taskflow.dto.AuthResponse;
import com.taskflow.dto.LoginRequest;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}
