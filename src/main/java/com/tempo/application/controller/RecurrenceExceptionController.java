package com.tempo.application.controller;

import com.tempo.application.error.ApiError;
import com.tempo.application.model.recurrenceException.RecurrenceException;
import com.tempo.application.model.recurrenceException.RecurrenceExceptionCreateDTO;
import com.tempo.application.model.recurrenceException.RecurrenceExceptionDTO;
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
     * Récupère toutes les exceptions de récurrence
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllRecurrenceExceptions() {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                ApiError error = new ApiError(HttpStatus.BAD_REQUEST, "User not found", "USER_NOT_FOUND");
                return ResponseEntity.badRequest().body(error);
            }

            // Récupérer les exceptions pour l'utilisateur (et celles sans séries)
            List<RecurrenceException> exceptions = recurrenceExceptionService.getAllRecurrenceExceptionsByUserId(user.getId());
            
            // Convertir toutes les exceptions en DTO
            List<RecurrenceExceptionDTO> allExceptions = exceptions.stream()
                .map(RecurrenceExceptionDTO::fromEntity)
                .toList();
            
            return ResponseEntity.ok(allExceptions);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving all recurrence exceptions: " + e.getMessage(), e);
            ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Crée une nouvelle exception de récurrence (période de pause)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createRecurrenceException(@Valid @RequestBody RecurrenceExceptionCreateDTO dto) {
        try {
            LoggerUtils.info(logger, "Creating new recurrence exception");

            RecurrenceException createdEntity = recurrenceExceptionService.createRecurrenceException(
                dto.getPauseStart(), dto.getPauseEnd()
            );
            
            RecurrenceExceptionDTO responseDto = RecurrenceExceptionDTO.fromEntity(createdEntity);
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            LoggerUtils.error(logger, "Validation error creating recurrence exception: " + e.getMessage(), e);
            ApiError error = new ApiError(HttpStatus.BAD_REQUEST, e.getMessage(), "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error creating recurrence exception: " + e.getMessage(), e);
            ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
                ApiError error = new ApiError(HttpStatus.BAD_REQUEST, "User not found", "USER_NOT_FOUND");
                return ResponseEntity.badRequest().body(error);
            }
            
            RecurrenceException exception = recurrenceExceptionService.getRecurrenceExceptionById(id);
            
            // Vérifier que l'utilisateur est autorisé à voir cette exception
            boolean isAuthorized = exception.getSeries().stream()
                .anyMatch(series -> series.getUser() != null && series.getUser().getId().equals(user.getId()));
            if (!isAuthorized) {
                ApiError error = new ApiError(HttpStatus.FORBIDDEN, "Access denied", "ACCESS_DENIED");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            RecurrenceExceptionDTO responseDto = RecurrenceExceptionDTO.fromEntity(exception);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving recurrence exception: " + e.getMessage(), e);
            ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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

    /**
     * Vérifie si une série peut être liée à une exception
     */
    @GetMapping("/check-link/{seriesId}/{exceptionId}")
    public ResponseEntity<?> checkSeriesLink(
            @PathVariable Integer seriesId,
            @PathVariable Integer exceptionId) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                ApiError error = new ApiError(HttpStatus.BAD_REQUEST, "User not found", "USER_NOT_FOUND");
                return ResponseEntity.badRequest().body(error);
            }

            boolean canLink = recurrenceExceptionService.canLinkSeriesToException(seriesId, exceptionId, user.getId());
            return ResponseEntity.ok(canLink);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error checking series link: " + e.getMessage(), e);
            ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Lie une série à une exception
     */
    @PostMapping("/link/{seriesId}/{exceptionId}")
    public ResponseEntity<?> linkSeriesToException(
            @PathVariable Integer seriesId,
            @PathVariable Integer exceptionId) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                ApiError error = new ApiError(HttpStatus.BAD_REQUEST, "User not found", "USER_NOT_FOUND");
                return ResponseEntity.badRequest().body(error);
            }

            recurrenceExceptionService.linkSeriesToException(seriesId, exceptionId, user.getId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            LoggerUtils.error(logger, "Validation error linking series to exception: " + e.getMessage(), e);
            ApiError error = new ApiError(HttpStatus.BAD_REQUEST, e.getMessage(), "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error linking series to exception: " + e.getMessage(), e);
            ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}