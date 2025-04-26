package com.tempo.application.controller;

import com.tempo.application.model.recurrenceException.RecurrenceException;
import com.tempo.application.model.user.User;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.service.RecurrenceExceptionService;
import com.tempo.application.utils.LoggerUtils;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recurrenceexception")
public class RecurrenceExceptionController {

    private static final Logger logger = LoggerUtils.getLogger(RecurrenceExceptionController.class);
    
    @Autowired
    private RecurrenceExceptionService recurrenceExceptionService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Crée une nouvelle exception de récurrence (période de pause)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createRecurrenceException(@Valid @RequestBody RecurrenceException recurrenceException) {
        try {
            LoggerUtils.info(logger, "Creating new recurrence exception");
            
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                LoggerUtils.error(logger, "User not found for email: " + email);
                return ResponseEntity.badRequest().body("User not found");
            }
            
            // Vérifier que l'utilisateur est propriétaire de la série
            if (recurrenceException.getSeries().getUser() != null && 
                !recurrenceException.getSeries().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only create exceptions for your own series");
            }
            
            RecurrenceException createdException = recurrenceExceptionService.createRecurrenceException(recurrenceException);
            return new ResponseEntity<>(createdException, HttpStatus.CREATED);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error creating recurrence exception: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating recurrence exception: " + e.getMessage());
        }
    }
    
    /**
     * Récupère une exception de récurrence par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRecurrenceException(@PathVariable Long id) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            RecurrenceException exception = recurrenceExceptionService.getRecurrenceExceptionById(id);
            
            // Vérifier que l'utilisateur est autorisé à voir cette exception
            if (!exception.getSeries().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            
            return ResponseEntity.ok(exception);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving recurrence exception: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving recurrence exception: " + e.getMessage());
        }
    }
    
    /**
     * Met à jour une exception de récurrence existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecurrenceException(
            @PathVariable Long id, 
            @Valid @RequestBody RecurrenceException recurrenceException) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            RecurrenceException updatedException = recurrenceExceptionService.updateRecurrenceException(
                id, recurrenceException, user.getId());
            
            return ResponseEntity.ok(updatedException);
        } catch (IllegalArgumentException e) {
            LoggerUtils.error(logger, "Access denied updating recurrence exception: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error updating recurrence exception: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating recurrence exception: " + e.getMessage());
        }
    }
    
    /**
     * Supprime une exception de récurrence
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecurrenceException(@PathVariable Long id) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            recurrenceExceptionService.deleteRecurrenceException(id, user.getId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            LoggerUtils.error(logger, "Access denied deleting recurrence exception: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error deleting recurrence exception: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting recurrence exception: " + e.getMessage());
        }
    }
    
    /**
     * Récupère toutes les exceptions de récurrence pour une série donnée
     */
    @GetMapping("/series/{seriesId}")
    public ResponseEntity<?> getExceptionsBySeries(@PathVariable Long seriesId) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            List<RecurrenceException> exceptions = recurrenceExceptionService.getExceptionsBySeriesId(seriesId, user.getId());
            return ResponseEntity.ok(exceptions);
        } catch (IllegalArgumentException e) {
            LoggerUtils.error(logger, "Access denied retrieving exceptions: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving recurrence exceptions: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving recurrence exceptions: " + e.getMessage());
        }
    }
}