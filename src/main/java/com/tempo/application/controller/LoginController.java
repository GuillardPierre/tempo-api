package com.tempo.application.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tempo.application.config.JwtUtils;
import com.tempo.application.exceptions.TokenRefreshException;
import com.tempo.application.model.refreshToken.RefreshToken;
import com.tempo.application.model.refreshToken.DTO.RefreshTokenDTO;
import com.tempo.application.model.user.User;
import com.tempo.application.model.user.UserCreateDto;
import com.tempo.application.model.user.UserDto;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.service.RefreshTokenService;
import com.tempo.application.service.UserService;
import com.tempo.application.utils.LoggerUtils;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping(path="/user")
public class LoginController {
    
    private static final Logger logger = LoggerUtils.getLogger(LoginController.class);
    
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
                Map<String, Object> authData = new HashMap<>();
                authData.put("token", jwtUtils.generateToken(request.getEmail()));
                authData.put("refreshToken", refreshTokenService.createRefreshToken(request.getEmail()).getToken());
                authData.put("type", "Bearer");
                return ResponseEntity.ok(authData);
            }
            return ResponseEntity.status(401).body("Invalid email or password.");
        } catch (AuthenticationException e) {
            LoggerUtils.error(logger, "Authentication failed: " + e.getMessage(), e);
            return ResponseEntity.status(401).body("Invalid email or password.");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        String requestRefreshToken = refreshTokenDTO.getToken();

        try {
            return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateToken(user.getEmail());
                    Map<String, Object> authData = new HashMap<>();
                    authData.put("refreshToken", requestRefreshToken);
                    authData.put("token", token);
                    authData.put("type", "Bearer");
                    return ResponseEntity.ok(authData);
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found in database"));
        } catch (TokenRefreshException e) {
            LoggerUtils.error(logger, "Token refresh failed: " + e.getMessage(), e);
            Map<String, String> errorData = new HashMap<>();
            errorData.put("error", "Refresh token expired");
            errorData.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorData);
        }
    }
}
