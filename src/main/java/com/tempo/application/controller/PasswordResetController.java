package com.tempo.application.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tempo.application.model.passwordReset.dto.PasswordResetConfirmDTO;
import com.tempo.application.model.passwordReset.dto.PasswordResetRequestDTO;
import com.tempo.application.service.PasswordResetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/user/password-reset")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<?> request(@Valid @RequestBody PasswordResetRequestDTO dto) {
        passwordResetService.requestReset(dto.getEmail());
        return ResponseEntity.ok(Collections.singletonMap("message", "Si un compte existe, un code a été envoyé."));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@Valid @RequestBody PasswordResetConfirmDTO dto) {
        boolean ok = passwordResetService.confirm(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        if (!ok) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Code invalide ou expiré"));
        }
        return ResponseEntity.ok(Collections.singletonMap("message", "Mot de passe mis à jour"));
    }
}
