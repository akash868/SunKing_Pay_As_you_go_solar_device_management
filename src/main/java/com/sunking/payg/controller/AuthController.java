package com.sunking.payg.controller;

import com.sunking.payg.dto.request.LoginRequest;
import com.sunking.payg.dto.response.ApiResponse;
import com.sunking.payg.dto.response.AuthResponse;
import com.sunking.payg.entity.AppUser;
import com.sunking.payg.repository.AppUserRepository;
import com.sunking.payg.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and token management")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AppUserRepository appUserRepository;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with username/password and receive a JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AppUser appUser = appUserRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = tokenProvider.generateToken(userDetails, appUser.getRole().name());

        AuthResponse response = AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationMs() / 1000)
                .role(appUser.getRole().name())
                .username(appUser.getUsername())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
