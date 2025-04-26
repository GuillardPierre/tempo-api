package com.tempo.application.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tempo.application.model.schedule.ScheduleEntryDTO;
import com.tempo.application.model.user.User;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.service.ScheduleService;
import com.tempo.application.utils.LoggerUtils;

/**
 * Contrôleur pour gérer les opérations combinées sur le planning (Worktime et WorktimeSeries)
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private static final Logger logger = LoggerUtils.getLogger(ScheduleController.class);
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Récupère toutes les entrées de planification (Worktime et WorktimeSeries) d'un utilisateur pour une date spécifique
     * 
     * @param date La date au format YYYY-MM-DD
     * @return Une liste combinée des entrées de planification
     */
    @GetMapping("/{date}")
    public ResponseEntity<?> getUserScheduleByDate(@PathVariable String date) {
        try {
            LoggerUtils.info(logger, "Fetching user schedule for date: " + date);
            
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                LoggerUtils.error(logger, "User not found for email: " + email);
                return ResponseEntity.badRequest().body("User not found");
            }
            
            // Convertir la chaîne de date en objet LocalDate
            LocalDate localDate;
            try {
                localDate = LocalDate.parse(date);
            } catch (Exception e) {
                LoggerUtils.error(logger, "Invalid date format: " + date);
                return ResponseEntity.badRequest().body("Invalid date format. Use YYYY-MM-DD format.");
            }
            
            // Récupérer les entrées de planification combinées
            List<ScheduleEntryDTO> scheduleEntries = scheduleService.getUserScheduleByDate(localDate, user.getId().longValue());
            
            return ResponseEntity.ok(scheduleEntries);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving user schedule: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving user schedule: " + e.getMessage());
        }
    }
}