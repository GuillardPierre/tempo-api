package com.tempo.application.service;

import com.tempo.application.model.recurrenceException.RecurrenceException;
import com.tempo.application.model.user.User;
import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import com.tempo.application.repository.RecurrenceExceptionRepository;
import com.tempo.application.repository.WorkTimeSeriesRepository;
import com.tempo.application.utils.LoggerUtils;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecurrenceExceptionService {

    private static final Logger logger = LoggerUtils.getLogger(RecurrenceExceptionService.class);

    @Autowired
    private RecurrenceExceptionRepository recurrenceExceptionRepository;

    @Autowired
    private WorkTimeSeriesRepository workTimeSeriesRepository;

    /**
     * Crée une nouvelle exception de récurrence (période de pause)
     * 
     * @param recurrenceException La requête de création d'exception
     * @return L'exception créée avec son ID
     */
    public RecurrenceException createRecurrenceException(RecurrenceException recurrenceException) {
        LoggerUtils.info(logger, "Creating new recurrence exception");
        
        // Vérifier que les dates sont cohérentes
        if (recurrenceException.getPauseStart() != null && recurrenceException.getPauseEnd() != null &&
            recurrenceException.getPauseStart().isAfter(recurrenceException.getPauseEnd())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }
        
        // Vérifier que la série existe
        WorktimeSeries series = workTimeSeriesRepository.findById(recurrenceException.getSeries().getId().intValue())
            .orElseThrow(() -> new RuntimeException("Worktime series not found with id: " + recurrenceException.getSeries().getId()));
        
        recurrenceException.setSeries(series);
        
        return recurrenceExceptionRepository.save(recurrenceException);
    }
    
    /**
     * Met à jour une exception de récurrence existante
     * 
     * @param id L'ID de l'exception à mettre à jour
     * @param recurrenceExceptionDetails Les nouvelles données
     * @param userId L'ID de l'utilisateur pour vérification d'autorisation
     * @return L'exception mise à jour
     */
    public RecurrenceException updateRecurrenceException(Long id, RecurrenceException recurrenceExceptionDetails, Integer userId) {
        LoggerUtils.info(logger, "Updating recurrence exception with id: " + id);
        
        RecurrenceException existingException = recurrenceExceptionRepository.findById(id.intValue())
            .orElseThrow(() -> new RuntimeException("Recurrence exception not found with id: " + id));
        
        // Vérifier que l'utilisateur est le propriétaire de la série associée
        if (!existingException.getSeries().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only update exceptions for your own series.");
        }
        
        // Vérifier la cohérence des dates
        LocalDateTime start = recurrenceExceptionDetails.getPauseStart() != null ? 
                recurrenceExceptionDetails.getPauseStart() : existingException.getPauseStart();
        LocalDateTime end = recurrenceExceptionDetails.getPauseEnd() != null ? 
                recurrenceExceptionDetails.getPauseEnd() : existingException.getPauseEnd();
                
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }
        
        // Mise à jour des propriétés
        existingException.setPauseStart(start);
        existingException.setPauseEnd(end);
        
        return recurrenceExceptionRepository.save(existingException);
    }
    
    /**
     * Supprime une exception de récurrence
     * 
     * @param id L'ID de l'exception à supprimer
     * @param userId L'ID de l'utilisateur pour vérification d'autorisation
     */
    public void deleteRecurrenceException(Long id, Integer userId) {
        LoggerUtils.info(logger, "Deleting recurrence exception with id: " + id);
        
        RecurrenceException exception = recurrenceExceptionRepository.findById(id.intValue())
            .orElseThrow(() -> new RuntimeException("Recurrence exception not found with id: " + id));
        
        // Vérifier que l'utilisateur est le propriétaire de la série associée
        if (!exception.getSeries().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete exceptions for your own series.");
        }
        
        recurrenceExceptionRepository.delete(exception);
    }
    
    /**
     * Récupère une exception de récurrence par son ID
     * 
     * @param id L'ID de l'exception à récupérer
     * @return L'exception trouvée
     */
    public RecurrenceException getRecurrenceExceptionById(Long id) {
        LoggerUtils.info(logger, "Fetching recurrence exception with id: " + id);
        
        return recurrenceExceptionRepository.findById(id.intValue())
            .orElseThrow(() -> new RuntimeException("Recurrence exception not found with id: " + id));
    }
    
    /**
     * Récupère toutes les exceptions de récurrence pour une série donnée
     * 
     * @param seriesId L'ID de la série
     * @param userId L'ID de l'utilisateur pour vérification d'autorisation
     * @return La liste des exceptions pour cette série
     */
    public List<RecurrenceException> getExceptionsBySeriesId(Long seriesId, Integer userId) {
        LoggerUtils.info(logger, "Fetching recurrence exceptions for series id: " + seriesId);
        
        WorktimeSeries series = workTimeSeriesRepository.findById(seriesId.intValue())
            .orElseThrow(() -> new RuntimeException("Worktime series not found with id: " + seriesId));
            
        // Vérifier que l'utilisateur est le propriétaire de la série
        if (!series.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only access exceptions for your own series.");
        }
        
        return series.getExceptions();
    }
}