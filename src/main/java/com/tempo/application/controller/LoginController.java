package com.tempo.application.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tempo.application.config.JwtUtils;
import com.tempo.application.model.refreshToken.RefreshToken;
import com.tempo.application.model.refreshToken.DTO.RefreshTokenDTO;
import com.tempo.application.model.user.User;
import com.tempo.application.model.user.UserCreateDto;
import com.tempo.application.model.user.UserDto;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.service.RefreshTokenService;
import com.tempo.application.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping(path="/user")
public class LoginController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@Valid @RequestBody UserCreateDto request) { 
        try {
            UserDto createdUser = userService.register(request);
            return ResponseEntity.ok(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User request) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            if (authentication.isAuthenticated()) {
                System.out.println("COUCOU");
                 Map<String, Object> authData = new HashMap<>();
                authData.put("token", jwtUtils.generateToken(request.getEmail()));
                authData.put("refreshToken", refreshTokenService.createRefreshToken(request.getEmail()).getToken());
                authData.put("type", "Bearer");
                return ResponseEntity.ok(authData);
            }
            return ResponseEntity.status(401).body("Invalid email or password.");
        } catch (AuthenticationException e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid email or password.");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        try {
            return refreshTokenService.findByToken(refreshTokenDTO.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateToken(user.getEmail());
                    Map<String, Object> authData = new HashMap<>();
                    authData.put("refreshToken", refreshTokenDTO.getToken());
                    authData.put("token", token);
                    authData.put("type", "Bearer");
                    return ResponseEntity.ok(authData);
                })
                .orElseGet(() -> {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("error", "Refresh token not found");
                    return ResponseEntity.status(403).body(errorData);
                });
        } catch (AuthenticationException e) {
            System.out.println("Authentication failed: " + e.getMessage());
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", "Refresh token is invalid");
            return ResponseEntity.status(403).body(errorData);
        }
    }
}
