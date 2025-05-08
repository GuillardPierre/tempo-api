package com.tempo.application.controller;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tempo.application.model.user.User;
import com.tempo.application.model.worktime.Worktime;
import com.tempo.application.model.worktime.DTO.WorktimeRequestDTO;
import com.tempo.application.model.worktime.DTO.WorktimeResponseDTO;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.service.WorktimeService;
import com.tempo.application.utils.LoggerUtils;

@RestController
@RequestMapping("/worktime")
public class WorktimeController {
    
    private static final Logger logger = LoggerUtils.getLogger(WorktimeController.class);
    
    @Autowired
    private WorktimeService worktimeService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/create")
    public ResponseEntity<?> createWorktime(@RequestBody WorktimeRequestDTO worktimeRequest) {
        try {
            LoggerUtils.info(logger, "Creating new worktime entry");
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                LoggerUtils.error(logger, "User not found for email: " + email);
                return ResponseEntity.badRequest().body("User not found");
            }
            
            Worktime createdWorktime = worktimeService.createWorktime(worktimeRequest, user.getId());
            WorktimeResponseDTO responseDTO = WorktimeResponseDTO.fromEntity(createdWorktime);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error creating worktime: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating worktime: " + e.getMessage());
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<?> getUserWorktimes() {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            List<Worktime> worktimes = worktimeService.getAllUserWorktimes(user.getId());
            return ResponseEntity.ok(worktimes);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving user worktimes: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving worktimes: " + e.getMessage());
        }
    }

    // Route /user/{date} déplacée vers ScheduleController
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getWorktime(@PathVariable int id) {
        try {
            Worktime worktime = worktimeService.getWorktimeById(id);
            
            // Vérifier que l'utilisateur connecté est le propriétaire du worktime
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (!worktime.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Access denied");
            }
            
            return ResponseEntity.ok(worktime);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error retrieving worktime: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving worktime: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorktime(@PathVariable int id, @RequestBody WorktimeRequestDTO worktimeRequest) {
        try {
            // Vérifier que l'utilisateur connecté est le propriétaire du worktime
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            Worktime worktime = worktimeService.getWorktimeById(id);
            if (!worktime.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Access denied");
            }
            
            Worktime updatedWorktime = worktimeService.updateWorktime(worktimeRequest, id);
            WorktimeResponseDTO responseDTO = WorktimeResponseDTO.fromEntity(updatedWorktime);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error updating worktime: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating worktime: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorktime(@PathVariable int id) {
        try {
            // Vérifier que l'utilisateur connecté est le propriétaire du worktime
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            Worktime worktime = worktimeService.getWorktimeById(id);
            if (!worktime.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Access denied");
            }
            
            worktimeService.deleteWorktimeById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LoggerUtils.error(logger, "Error deleting worktime: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting worktime: " + e.getMessage());
        }
    }
}
