package com.tempo.application.controller;

import java.time.LocalDate;
import java.util.Collections;
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

import com.tempo.application.model.schedule.ScheduleDateEntryDTO;
import com.tempo.application.model.schedule.ScheduleEntryDTO;
import com.tempo.application.model.schedule.ScheduleThreeDaysDTO;
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
    @GetMapping("/day/{date}")
    public ResponseEntity<?> getUserScheduleByDate(@PathVariable String date) {
        try {
            LoggerUtils.info(logger, "Fetching user schedule for date: " + date);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                LoggerUtils.error(logger, "User not found for email: " + email);
                return ResponseEntity.badRequest().body("User not found");
            }
            
            LocalDate localDate;
            try {
                localDate = LocalDate.parse(date);
            } catch (Exception e) {
                LoggerUtils.error(logger, "Invalid date format: " + date);
                return ResponseEntity.badRequest().body("Invalid date format. Use YYYY-MM-DD format.");
            }
            List<ScheduleEntryDTO> yesterday = scheduleService.getUserScheduleByDate(localDate.minusDays(1), user.getId());
            List<ScheduleEntryDTO> today = scheduleService.getUserScheduleByDate(localDate, user.getId());
            List<ScheduleEntryDTO> tomorrow = scheduleService.getUserScheduleByDate(localDate.plusDays(1), user.getId());
            ScheduleThreeDaysDTO scheduleThreeDays = ScheduleThreeDaysDTO.builder()
                .yesterday(yesterday)
                .today(today)
                .tomorrow(tomorrow)
                .build();
            return ResponseEntity.ok(scheduleThreeDays);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving user schedule: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving user schedule: " + e.getMessage());
        }
    }

    @GetMapping("/month/{month}")
    public ResponseEntity<?> getUserScheduleByMonth(@PathVariable String month) {
        LoggerUtils.info(logger, "Fetching user schedule for month: " + month);
            
        try {
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
                localDate = LocalDate.parse(month);
            } catch (Exception e) {
                LoggerUtils.error(logger, "Invalid month format: " + month);
                return ResponseEntity.badRequest().body("Invalid month format. Use YYYY-MM format.");
            }

            List<ScheduleDateEntryDTO> scheduleEntries = scheduleService.getUserScheduleByMonth(localDate, user.getId());
            return ResponseEntity.ok(scheduleEntries);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving monthly schedule: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving monthly schedule: " + e.getMessage());
        }
    }
}