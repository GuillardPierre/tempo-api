package com.tempo.application.service;

import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import com.tempo.application.model.category.Category;
import com.tempo.application.model.user.User;
import com.tempo.application.model.recurrenceException.RecurrenceException;
import com.tempo.application.repository.CategoryRepository;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.repository.WorkTimeSeriesRepository;
import com.tempo.application.repository.RecurrenceExceptionRepository;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tempo.application.utils.LoggerUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.cache.annotation.CacheEvict;

@Service
public class WorktimeSeriesService {

    private static final Logger logger = LoggerUtils.getLogger(WorktimeSeriesService.class);

    @Autowired
    private WorkTimeSeriesRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecurrenceExceptionRepository recurrenceExceptionRepository;

    /**
     * Crée une nouvelle série de créneaux horaires
     * 
     * @param request La série à créer
     * @return La série créée avec son ID généré
     */
    @CacheEvict(value = "categoryStats", allEntries = true)
    public WorktimeSeries createWorktimeSeries(WorktimeSeries request) {
        LoggerUtils.info(logger, "Creating new worktime series");
        
        if (request.getStartDate() != null && request.getEndDate() != null && 
            request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        // Vérification des nouveaux champs startTime et endTime
        if (request.getStartHour() == null) {
            throw new IllegalArgumentException("Start time is required.");
        }
        
        if (request.getEndHour() == null) {
            throw new IllegalArgumentException("End time is required.");
        }
        
        if (request.getStartHour().isAfter(request.getEndHour())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        if (!userRepository.existsById(request.getUser().getId())) {
            throw new IllegalArgumentException("User not found.");
        }

        // Correction : charger la catégorie complète depuis la base pour garantir que le nom est bien présent
        Category category = categoryRepository.findById(request.getCategory().getId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        request.setCategory(category);
        
        // Vérifier que la règle de récurrence est fournie
        if (request.getRecurrence() == null) {
            throw new IllegalArgumentException("Recurrence rule is required.");
        }

        // Si ignoreExceptions est false, chercher les exceptions qui se chevauchent
        if (!Boolean.TRUE.equals(request.getIgnoreExceptions())) {
            List<RecurrenceException> overlappingExceptions = recurrenceExceptionRepository.findOverlappingExceptions(
                request.getStartDate(),
                request.getEndDate() != null ? request.getEndDate() : LocalDateTime.now().plusYears(100)
            );

            // Lier les exceptions qui se chevauchent à la série
            request.setExceptions(new ArrayList<>(overlappingExceptions));
            
            // Mettre à jour la relation bidirectionnelle
            for (RecurrenceException exception : overlappingExceptions) {
                exception.getSeries().add(request);
            }
        }

        WorktimeSeries savedSeries = repository.save(request);

        // Sauvegarder les exceptions mises à jour si nécessaire
        if (!Boolean.TRUE.equals(request.getIgnoreExceptions()) && !request.getExceptions().isEmpty()) {
            recurrenceExceptionRepository.saveAll(request.getExceptions());
        }

        return savedSeries;
    }
    
    /**
     * Met à jour une série de créneaux horaires existante
     * 
     * @param id L'identifiant de la série à modifier
     * @param request La série avec les nouvelles valeurs
     * @return La série mise à jour
     */
    @CacheEvict(value = "categoryStats", allEntries = true)
    public WorktimeSeries updateWorktimeSeries(Long id, WorktimeSeries request) {
        LoggerUtils.info(logger, "Updating worktime series with id: " + id);
        
        WorktimeSeries existingSeries = repository.findById(id.intValue())
            .orElseThrow(() -> new RuntimeException("Worktime series not found with id: " + id));
        
        if (request.getStartDate() != null && request.getEndDate() != null && 
            request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        // Vérification des nouveaux champs startTime et endTime
        if (request.getStartHour() != null && request.getEndHour() != null && 
            request.getStartHour().isAfter(request.getEndHour())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        if (!userRepository.existsById(request.getUser().getId())) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!categoryRepository.existsById(request.getCategory().getId())) {
            throw new IllegalArgumentException("Category not found.");
        }
        
        // Vérifier que l'utilisateur est le propriétaire de cette série
        if (!existingSeries.getUser().getId().equals(request.getUser().getId())) {
            throw new IllegalArgumentException("You can only update your own worktime series.");
        }
        
        // Mise à jour des propriétés
        if (request.getRecurrence() != null) {
            existingSeries.setRecurrence(request.getRecurrence());
        }

        // Récupérarion de la catégorie existante pour renvoyer ID + nom
        Category category = categoryRepository.findById(request.getCategory().getId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        existingSeries.setCategory(category);
        existingSeries.setStartDate(request.getStartDate());
        existingSeries.setEndDate(request.getEndDate());
        
        if (request.getStartHour() != null) {
            existingSeries.setStartHour(request.getStartHour());
        }
        if (request.getEndHour() != null) {
            existingSeries.setEndHour(request.getEndHour());
        }

        // Gestion des exceptions de récurrence
        if (request.getIgnoreExceptions() != null) {
            existingSeries.setIgnoreExceptions(request.getIgnoreExceptions());
            
            if (Boolean.TRUE.equals(request.getIgnoreExceptions())) {
                // Si on ignore maintenant les exceptions, on supprime toutes les liaisons
                for (RecurrenceException exception : existingSeries.getExceptions()) {
                    exception.getSeries().remove(existingSeries);
                }
                existingSeries.getExceptions().clear();
            } else {
                // Si on ne les ignore plus, on cherche les exceptions qui se chevauchent
                List<RecurrenceException> overlappingExceptions = recurrenceExceptionRepository.findOverlappingExceptions(
                    existingSeries.getStartDate(),
                    existingSeries.getEndDate() != null ? existingSeries.getEndDate() : LocalDateTime.now().plusYears(100)
                );

                // Mettre à jour les liaisons
                existingSeries.getExceptions().clear();
                existingSeries.getExceptions().addAll(overlappingExceptions);
                
                // Mettre à jour la relation bidirectionnelle
                for (RecurrenceException exception : overlappingExceptions) {
                    exception.getSeries().add(existingSeries);
                }
            }
        }
        
        WorktimeSeries savedSeries = repository.save(existingSeries);

        // Sauvegarder les exceptions mises à jour si nécessaire
        if (!Boolean.TRUE.equals(savedSeries.getIgnoreExceptions()) && !savedSeries.getExceptions().isEmpty()) {
            recurrenceExceptionRepository.saveAll(savedSeries.getExceptions());
        }

        return savedSeries;
    }
    
    /**
     * Supprime une série de créneaux horaires
     * 
     * @param id L'identifiant de la série à supprimer
     * @param userId L'identifiant de l'utilisateur (pour vérification d'autorisation)
     */
    @CacheEvict(value = "categoryStats", allEntries = true)
    public void deleteWorktimeSeries(Long id, Integer userId) {
        LoggerUtils.info(logger, "Deleting worktime series with id: " + id);
        
        WorktimeSeries existingSeries = repository.findById(id.intValue())
            .orElseThrow(() -> new RuntimeException("Worktime series not found with id: " + id));
        
        // Vérifier que l'utilisateur est le propriétaire de cette série
        if (!existingSeries.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own worktime series.");
        }
        
        repository.delete(existingSeries);
    }
    
    /**
     * Récupère une série de créneaux horaires par son identifiant
     * 
     * @param id L'identifiant de la série à récupérer
     * @return La série trouvée
     */
    public WorktimeSeries getWorktimeSeriesById(Long id) {
        LoggerUtils.info(logger, "Fetching worktime series with id: " + id);
        
        return repository.findById(id.intValue())
            .orElseThrow(() -> new RuntimeException("Worktime series not found with id: " + id));
    }
    
    /**
     * Récupère toutes les séries de créneaux horaires d'un utilisateur
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return La liste des séries associées à cet utilisateur
     */
    public List<WorktimeSeries> getAllWorkTimeSeriesByUserId(Integer userId) {
        LoggerUtils.info(logger, "Fetching all worktime series for user id: " + userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found."));
        
        return repository.findByUser(user);
    }
    
    /**
     * Récupère toutes les séries de créneaux horaires actifs d'un utilisateur
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return La liste des séries actives associées à cet utilisateur
     */
    public List<WorktimeSeries> getAllActiveWorkTimeSeriesByUserId(Integer userId) {
        LoggerUtils.info(logger, "Fetching active worktime series for user id: " + userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found."));
        
        return repository.findByUser(user);
    }
    
    /**
     * Récupère toutes les séries de créneaux horaires actives pour une date spécifique et un utilisateur
     * 
     * @param date La date pour laquelle vérifier l'activité des séries
     * @param userId L'identifiant de l'utilisateur
     * @return La liste des séries actives à cette date pour cet utilisateur
     */
    public List<WorktimeSeries> getActiveWorkTimeSeriesForDateAndUser(LocalDate date, Integer userId) {
        LoggerUtils.info(logger, "Fetching active worktime series for date: " + date + " and user id: " + userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        LocalDateTime targetDate = date.atStartOfDay();
        List<WorktimeSeries> allActiveSeries = repository.findByUser(user);
        return allActiveSeries.stream()
            .filter(series -> {
                boolean afterStart = series.getStartDate().isBefore(targetDate) || series.getStartDate().isEqual(targetDate);
                boolean beforeEnd = series.getEndDate() == null || targetDate.isBefore(series.getEndDate()) || targetDate.isEqual(series.getEndDate());
                boolean matchesRecurrence = series.getRecurrence() != null;
                return afterStart && beforeEnd && matchesRecurrence;
            })
            .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les séries de créneaux horaires actives pour un mois spécifique et un utilisateur
     * 
     * @param date Une date dans le mois pour lequel vérifier l'activité des séries
     * @param userId L'identifiant de l'utilisateur
     * @return La liste des séries actives pour ce mois et cet utilisateur
     */
    public List<WorktimeSeries> getActiveWorkTimeSeriesForMonthAndUser(LocalDate date, Integer userId) {
        LoggerUtils.info(logger, "Fetching active worktime series for month: " + date.getMonth() + " and user id: " + userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = date.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        List<WorktimeSeries> allActiveSeries = repository.findByUser(user);
        return allActiveSeries.stream()
            .filter(series -> {
                boolean afterStart = series.getStartDate().isBefore(startOfNextMonth) || series.getStartDate().isEqual(startOfNextMonth);
                boolean beforeEnd = series.getEndDate() == null || startOfMonth.isBefore(series.getEndDate()) || startOfMonth.isEqual(series.getEndDate());
                boolean matchesRecurrence = series.getRecurrence() != null;
                return afterStart && beforeEnd && matchesRecurrence;
            })
            .collect(Collectors.toList());
    }
}
