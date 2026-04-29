package com.minimonk.user.auth;

import com.minimonk.security.JwtSupport;
import com.minimonk.user.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtSupport jwtSupport;

    public AuthController(AppUserRepository users, PasswordEncoder passwordEncoder, JwtSupport jwtSupport) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtSupport = jwtSupport;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var user = users.findByUsername(request.username())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        var roles = new ArrayList<>(user.getRoles());
        var token = jwtSupport.issue(user.getId(), user.getUsername(), roles);
        return new LoginResponse(token, user.getId(), user.getUsername(), roles);
    }
}
