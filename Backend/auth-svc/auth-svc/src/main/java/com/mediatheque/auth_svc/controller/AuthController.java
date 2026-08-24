package com.mediatheque.auth_svc.controller;

import com.mediatheque.auth_svc.dto.AuthResponse;
import com.mediatheque.auth_svc.dto.LoginRequest;
import com.mediatheque.auth_svc.dto.RegisterRequest;
import com.mediatheque.auth_svc.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}