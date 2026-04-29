package com.minimonk.user.auth;

import com.minimonk.security.JwtSupport;
import com.minimonk.user.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtSupport jwtSupport;

    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder, JwtSupport jwtSupport) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtSupport = jwtSupport;
    }

    public LoginResponse login(LoginRequest request) {
        var user = users.findByUsername(request.username())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        var roles = new ArrayList<>(user.getRoles());
        var token = jwtSupport.issue(user.getId(), user.getUsername(), roles);
        return new LoginResponse(token, user.getId(), user.getUsername(), roles);
    }
}
