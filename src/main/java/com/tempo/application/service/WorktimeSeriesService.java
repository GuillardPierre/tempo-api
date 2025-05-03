package com.tempo.application.service;

import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import com.tempo.application.model.user.User;
import com.tempo.application.repository.CategoryRepository;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.repository.WorkTimeSeriesRepository;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tempo.application.utils.LoggerUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorktimeSeriesService {

    private static final Logger logger = LoggerUtils.getLogger(WorktimeSeriesService.class);

    @Autowired
    private WorkTimeSeriesRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Crée une nouvelle série de créneaux horaires
     * 
     * @param request La série à créer
     * @return La série créée avec son ID généré
     */
    public WorktimeSeries createWorktimeSeries(WorktimeSeries request) {
        LoggerUtils.info(logger, "Creating new worktime series");
        
        if (request.getStartDate() != null && request.getEndDate() != null && 
            request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        // Vérification des nouveaux champs startTime et endTime
        if (request.getStartTime() == null) {
            throw new IllegalArgumentException("Start time is required.");
        }
        
        if (request.getEndTime() == null) {
            throw new IllegalArgumentException("End time is required.");
        }
        
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        if (!userRepository.existsById(request.getUser().getId())) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!categoryRepository.existsById(request.getCategory().getId())) {
            throw new IllegalArgumentException("Category not found.");
        }
        
        // Vérifier que la règle de récurrence est fournie
        if (request.getRecurrence() == null) {
            throw new IllegalArgumentException("Recurrence rule is required.");
        }

        return repository.save(request);
    }
    
    /**
     * Met à jour une série de créneaux horaires existante
     * 
     * @param id L'identifiant de la série à modifier
     * @param request La série avec les nouvelles valeurs
     * @return La série mise à jour
     */
    public WorktimeSeries updateWorktimeSeries(Long id, WorktimeSeries request) {
        LoggerUtils.info(logger, "Updating worktime series with id: " + id);
        
        WorktimeSeries existingSeries = repository.findById(id.intValue())
            .orElseThrow(() -> new RuntimeException("Worktime series not found with id: " + id));
        
        if (request.getStartDate() != null && request.getEndDate() != null && 
            request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        // Vérification des nouveaux champs startTime et endTime
        if (request.getStartTime() != null && request.getEndTime() != null && 
            request.getStartTime().isAfter(request.getEndTime())) {
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
        existingSeries.setCategory(request.getCategory());
        existingSeries.setStartDate(request.getStartDate());
        existingSeries.setEndDate(request.getEndDate());
        
        // Mise à jour des nouveaux champs startTime et endTime
        if (request.getStartTime() != null) {
            existingSeries.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            existingSeries.setEndTime(request.getEndTime());
        }
        
        existingSeries.setActive(request.isActive());
        
        return repository.save(existingSeries);
    }
    
    /**
     * Supprime une série de créneaux horaires
     * 
     * @param id L'identifiant de la série à supprimer
     * @param userId L'identifiant de l'utilisateur (pour vérification d'autorisation)
     */
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
        
        return repository.findByUserAndActiveTrue(user);
    }
    
    /**
     * Récupère toutes les séries de créneaux horaires actives pour une date spécifique et un utilisateur
     * 
     * @param date La date pour laquelle vérifier l'activité des séries
     * @param userId L'identifiant de l'utilisateur
     * @return La liste des séries actives à cette date pour cet utilisateur
     */
    public List<WorktimeSeries> getActiveWorkTimeSeriesForDateAndUser(LocalDate date, Long userId) {
        LoggerUtils.info(logger, "Fetching active worktime series for date: " + date + " and user id: " + userId);
        
        User user = userRepository.findById(userId.intValue());
        if (user == null) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        LocalDateTime targetDate = date.atStartOfDay();
        
        // Récupérer toutes les séries actives de cet utilisateur
        List<WorktimeSeries> allActiveSeries = repository.findByUserAndActiveTrue(user);
        
        // Filtrer pour ne garder que les séries qui sont actives à la date donnée
        return allActiveSeries.stream()
            .filter(series -> {
                // Série active si la date cible est après le début
                boolean afterStart = series.getStartDate().isBefore(targetDate) || series.getStartDate().isEqual(targetDate);
                
                // Et avant la fin (ou pas de fin définie)
                boolean beforeEnd = series.getEndDate() == null || targetDate.isBefore(series.getEndDate()) || targetDate.isEqual(series.getEndDate());
                
                // Vérifier la règle de récurrence (ici on fait une vérification simplifiée)
                // Note: Une implémentation complète nécessiterait de parser les règles RFC5545
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
    public List<WorktimeSeries> getActiveWorkTimeSeriesForMonthAndUser(LocalDate date, Long userId) {
        LoggerUtils.info(logger, "Fetching active worktime series for month: " + date.getMonth() + " and user id: " + userId);
        
        User user = userRepository.findById(userId.intValue());
        if (user == null) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        // Obtenir le premier jour du mois
        LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
        
        // Obtenir le premier jour du mois suivant
        LocalDateTime startOfNextMonth = date.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        
        // Récupérer toutes les séries actives de cet utilisateur
        List<WorktimeSeries> allActiveSeries = repository.findByUserAndActiveTrue(user);
        
        // Filtrer pour ne garder que les séries qui sont actives pendant le mois
        return allActiveSeries.stream()
            .filter(series -> {
                // Série active si la date de début est avant la fin du mois
                boolean startsBeforeEndOfMonth = series.getStartDate() == null || 
                                              series.getStartDate().isBefore(startOfNextMonth);
                
                // Et la date de fin est après le début du mois (ou pas de fin définie)
                boolean endsAfterStartOfMonth = series.getEndDate() == null || 
                                             series.getEndDate().isAfter(startOfMonth) || 
                                             series.getEndDate().isEqual(startOfMonth);
                
                // Vérifier que la règle de récurrence est définie
                boolean hasRecurrence = series.getRecurrence() != null && !series.getRecurrence().isEmpty();
                
                return startsBeforeEndOfMonth && endsAfterStartOfMonth && hasRecurrence;
            })
            .collect(Collectors.toList());
    }
}
