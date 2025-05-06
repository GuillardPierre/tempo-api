package com.tempo.application.controller;

import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import com.tempo.application.model.user.User;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.service.WorktimeSeriesService;
import com.tempo.application.utils.LoggerUtils;
import com.tempo.application.model.worktimeSeries.WorktimeSeriesResponseDTO;

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
@RequestMapping("/worktimeseries")
public class WorktimeSeriesController {

    private static final Logger logger = LoggerUtils.getLogger(WorktimeSeriesController.class);
    
    @Autowired
    private WorktimeSeriesService worktimeSeriesService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Crée une nouvelle série de créneaux horaires
     */
    @PostMapping("/create")
    public ResponseEntity<?> createWorktimeSeries(@Valid @RequestBody WorktimeSeries worktimeSeriesRequest) {
        try {
            LoggerUtils.info(logger, "Creating new worktime series");
            
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                LoggerUtils.error(logger, "User not found for email: " + email);
                return ResponseEntity.badRequest().body("User not found");
            }
            
            // S'assurer que l'utilisateur dans la requête correspond à l'utilisateur authentifié
            worktimeSeriesRequest.setUser(user);
            
            // Vérifier que la règle de récurrence est présente
            if (worktimeSeriesRequest.getRecurrence() == null || worktimeSeriesRequest.getRecurrence().isEmpty()) {
                return ResponseEntity.badRequest().body("Recurrence rule is required");
            }
            
            // Vérifier que les heures de début et de fin sont présentes
            if (worktimeSeriesRequest.getStartTime() == null) {
                return ResponseEntity.badRequest().body("Start time is required");
            }
            
            if (worktimeSeriesRequest.getEndTime() == null) {
                return ResponseEntity.badRequest().body("End time is required");
            }
            
            // Vérifier que l'heure de début est avant l'heure de fin
            if (worktimeSeriesRequest.getStartTime().isAfter(worktimeSeriesRequest.getEndTime())) {
                return ResponseEntity.badRequest().body("Start time must be before end time");
            }
            
            WorktimeSeries createdSeries = worktimeSeriesService.createWorktimeSeries(worktimeSeriesRequest);
            WorktimeSeriesResponseDTO responseDTO = WorktimeSeriesResponseDTO.fromEntity(createdSeries);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error creating worktime series: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating worktime series: " + e.getMessage());
        }
    }
    
    /**
     * Récupère une série de créneaux horaires par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getWorktimeSeries(@PathVariable Long id) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            WorktimeSeries workTimeSeries = worktimeSeriesService.getWorktimeSeriesById(id);
            
            // Vérifier que l'utilisateur est autorisé à voir cette série
            if (!workTimeSeries.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            
            WorktimeSeriesResponseDTO responseDTO = WorktimeSeriesResponseDTO.fromEntity(workTimeSeries);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving worktime series: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving worktime series: " + e.getMessage());
        }
    }
    
    /**
     * Met à jour une série de créneaux horaires existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorktimeSeries(@PathVariable Long id, @Valid @RequestBody WorktimeSeries worktimeSeriesRequest) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            // S'assurer que l'utilisateur dans la requête correspond à l'utilisateur authentifié
            worktimeSeriesRequest.setUser(user);
            
            WorktimeSeries updatedSeries = worktimeSeriesService.updateWorktimeSeries(id, worktimeSeriesRequest);
            WorktimeSeriesResponseDTO responseDTO = WorktimeSeriesResponseDTO.fromEntity(updatedSeries);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error updating worktime series: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating worktime series: " + e.getMessage());
        }
    }
    
    /**
     * Supprime une série de créneaux horaires
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorktimeSeries(@PathVariable Long id) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            worktimeSeriesService.deleteWorktimeSeries(id, user.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error deleting worktime series: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting worktime series: " + e.getMessage());
        }
    }
    
    /**
     * Récupère toutes les séries de créneaux horaires de l'utilisateur connecté
     */
    @GetMapping("/user")
    public ResponseEntity<?> getAllUserWorktimeSeries() {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            List<WorktimeSeries> seriesList = worktimeSeriesService.getAllWorkTimeSeriesByUserId(user.getId());
            return ResponseEntity.ok(seriesList);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving user worktime series: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving worktime series: " + e.getMessage());
        }
    }
    
    /**
     * Récupère toutes les séries de créneaux horaires actives de l'utilisateur connecté
     */
    @GetMapping("/user/active")
    public ResponseEntity<?> getAllActiveUserWorktimeSeries() {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            List<WorktimeSeries> activeSeriesList = worktimeSeriesService.getAllActiveWorkTimeSeriesByUserId(user.getId());
            return ResponseEntity.ok(activeSeriesList);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving user active worktime series: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving active worktime series: " + e.getMessage());
        }
    }
}
