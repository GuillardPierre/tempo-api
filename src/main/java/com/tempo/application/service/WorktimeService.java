package com.tempo.application.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tempo.application.model.category.Category;
import com.tempo.application.model.user.User;

import com.tempo.application.model.worktime.Worktime;
import com.tempo.application.model.worktime.DTO.WorktimeRequestDTO;
import com.tempo.application.repository.WorktimeRepository;
import com.tempo.application.repository.CategoryRepository;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.utils.LoggerUtils;


@Service
public class WorktimeService {
    
    private static final Logger logger = LoggerUtils.getLogger(WorktimeService.class);
    
    @Autowired
    WorktimeRepository worktimeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    public Worktime createWorktime(WorktimeRequestDTO worktimeRequest, Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Category category;
        
        if (worktimeRequest.getCategory().getId() == null) {
            // Rechercher la catégorie par nom et utilisateur
            category = categoryRepository.findByNameAndUser(worktimeRequest.getCategory().getName(), user);
            
            // Si la catégorie n'existe pas, la créer
            if (category == null) {
                LoggerUtils.info(logger, "Creating new category: " + worktimeRequest.getCategory().getName() + " for user: " + user.getEmail());
                category = Category.builder()
                    .name(worktimeRequest.getCategory().getName())
                    .user(user)
                    .build();
                category = categoryRepository.save(category);
            }
        } else {
            // Utiliser la catégorie spécifiée par ID
            category = categoryRepository.findByIdAndUser(worktimeRequest.getCategory().getId(), user)
                .orElseThrow(() -> new RuntimeException("Category not found or does not belong to this user"));
        }
        
        Worktime worktime = new Worktime();
        worktime.setStartTime(worktimeRequest.getStartTime());
        worktime.setEndTime(worktimeRequest.getEndTime());
        worktime.setCategory(category);
        worktime.setUser(user);
        return worktimeRepository.save(worktime);
    }

    public void deleteWorktimeById(int id) {
        if (worktimeRepository.existsById(id)) {
            worktimeRepository.deleteById(id);
        } else {
            throw new RuntimeException("Worktime with id " + id + " does not exist.");
        }
    }

    public void updateWorktime(WorktimeRequestDTO worktimeUpdateRequest, Integer id) {
        if (worktimeRepository.existsById(id)) {
            
            Worktime worktime = worktimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Worktime not found"));
            User user = worktime.getUser();
            Category category;
            
            // Vérifier si on doit utiliser une catégorie existante ou en créer une nouvelle
            if (worktimeUpdateRequest.getCategory().getId() == null) {
                category = categoryRepository.findByNameAndUser(worktimeUpdateRequest.getCategory().getName(), user);
                System.out.println("category:" + category);;
                if (category == null) {
                    LoggerUtils.info(logger, "Creating new category during worktime update: " + worktimeUpdateRequest.getCategory().getName());
                    category = Category.builder()
                        .name(worktimeUpdateRequest.getCategory().getName())
                        .user(user)
                        .build();
                    category = categoryRepository.save(category);
                }
            } else {
                // Utiliser la catégorie spécifiée par ID
                category = categoryRepository.findByIdAndUser(worktimeUpdateRequest.getCategory().getId(), user)
                    .orElseThrow(() -> new RuntimeException("Category not found or does not belong to this user"));
            }
                
            worktime.setStartTime(worktimeUpdateRequest.getStartTime());
            worktime.setEndTime(worktimeUpdateRequest.getEndTime());
            worktime.setCategory(category);
            worktimeRepository.save(worktime);
        } else {
            throw new RuntimeException("Worktime with id " + id + " does not exist.");
        }
    }

    public List<Worktime> getAllUserWorktimes(int userId) {
        return worktimeRepository.findByUserId(userId);
    }

    public Worktime getWorktimeById(int id) {
        Optional<Worktime> worktime = worktimeRepository.findById(id);
        if (worktime.isPresent()) {
            return worktime.get();
        } else {
            throw new RuntimeException("Worktime with id " + id + " does not exist.");
        }
    }
}
