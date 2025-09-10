package com.tempo.application.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tempo.application.config.JwtUtils;
import com.tempo.application.exceptions.TokenRefreshException;
import com.tempo.application.model.refreshToken.RefreshToken;
import com.tempo.application.model.refreshToken.DTO.RefreshTokenDTO;
import com.tempo.application.model.user.User;
import com.tempo.application.model.user.UserCreateDto;
import com.tempo.application.model.user.UserDto;
import com.tempo.application.service.RefreshTokenService;
import com.tempo.application.service.UserService;
import com.tempo.application.utils.LoggerUtils;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(path = "/user")
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
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            if (authentication.isAuthenticated()) {
                User user = userService.findByEmail(request.getEmail());
                Map<String, Object> authData = new HashMap<>();
                authData.put("token", jwtUtils.generateToken(request.getEmail()));
                authData.put("refreshToken", refreshTokenService.createRefreshToken(request.getEmail()).getToken());
                authData.put("type", "Bearer");
                authData.put("username", user.getUsername());
                authData.put("email", user.getEmail());
                authData.put("id", user.getId());
                return ResponseEntity.ok(authData);
            }
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Invalid email or password."));
        } catch (AuthenticationException e) {
            LoggerUtils.error(logger, "Authentication failed: " + e.getMessage(), e);
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Invalid email or password."));
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
                        // Générer un nouveau JWT token
                        String token = jwtUtils.generateToken(user.getEmail());

                        // Générer un nouveau refresh token (remplace l'ancien)
                        String newRefreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();

                        Map<String, Object> authData = new HashMap<>();
                        authData.put("refreshToken", newRefreshToken);
                        authData.put("token", token);
                        authData.put("type", "Bearer");
                        return ResponseEntity.ok(authData);
                    })
                    .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                            "Refresh token not found in database"));
        } catch (TokenRefreshException e) {
            LoggerUtils.error(logger, "Token refresh failed: " + e.getMessage(), e);
            Map<String, String> errorData = new HashMap<>();
            errorData.put("error", "Refresh token expired");
            errorData.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorData);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User connectedUser = userService.findByEmail(email);

        if (!connectedUser.getId().equals(id)) {
            return ResponseEntity.status(403)
                    .body(Collections.singletonMap("error", "Vous ne pouvez supprimer que votre propre compte."));
        }

        userService.deleteUserAndTokens(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "User deleted successfully"));
    }
}
