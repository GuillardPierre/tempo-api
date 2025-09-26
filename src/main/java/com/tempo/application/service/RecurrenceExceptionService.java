package com.tempo.application.service;

import com.tempo.application.model.recurrenceException.RecurrenceException;
import com.tempo.application.model.recurrenceException.ExceptionType;
import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import com.tempo.application.repository.RecurrenceExceptionRepository;
import com.tempo.application.repository.WorkTimeSeriesRepository;
import com.tempo.application.utils.LoggerUtils;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
     * @param pauseStart Date de début de la pause
     * @param pauseEnd   Date de fin de la pause
     * @return L'exception créée
     */
    @Transactional
    public RecurrenceException createRecurrenceException(LocalDateTime pauseStart, LocalDateTime pauseEnd) {
        LoggerUtils.info(logger, "Creating new recurrence exception");

        if (pauseStart != null && pauseEnd != null && pauseStart.isAfter(pauseEnd)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        // Vérifier s'il existe déjà des exceptions qui se chevauchent
        List<RecurrenceException> overlappingExceptions = recurrenceExceptionRepository.findOverlappingExceptions(
                pauseStart, pauseEnd);

        if (!overlappingExceptions.isEmpty()) {
            RecurrenceException existingException = overlappingExceptions.get(0);
            throw new IllegalArgumentException(
                    String.format("Une exception existe déjà qui chevauche cette période (du %s au %s).",
                            existingException.getPauseStart().toString(),
                            existingException.getPauseEnd().toString()));
        }

        // Créer l'exception
        RecurrenceException newException = RecurrenceException.builder()
                .pauseStart(pauseStart)
                .pauseEnd(pauseEnd)
                .series(new ArrayList<>())
                .build();

        // Sauvegarder l'exception pour obtenir son ID
        RecurrenceException savedException = recurrenceExceptionRepository.save(newException);

        // Rechercher toutes les séries qui pourraient être concernées par cette
        // exception
        List<WorktimeSeries> overlappingSeries = workTimeSeriesRepository.findAll().stream()
                .filter(series -> {
                    // Ignorer les séries qui ont ignoreExceptions à true
                    if (Boolean.TRUE.equals(series.getIgnoreExceptions())) {
                        return false;
                    }

                    // Vérifier si la série est active pendant la période de l'exception
                    boolean isActiveStart = series.getStartDate().isBefore(pauseEnd);
                    boolean isActiveEnd = series.getEndDate() == null || series.getEndDate().isAfter(pauseStart);

                    return isActiveStart && isActiveEnd;
                })
                .toList();

        if (!overlappingSeries.isEmpty()) {
            LoggerUtils.info(logger, String.format(
                    "Found %d series that overlap with this exception period",
                    overlappingSeries.size()));

            // Mettre à jour les relations
            savedException.getSeries().addAll(overlappingSeries);
            for (WorktimeSeries series : overlappingSeries) {
                series.getExceptions().add(savedException);
            }

            // Sauvegarder les mises à jour
            workTimeSeriesRepository.saveAll(overlappingSeries);
            savedException = recurrenceExceptionRepository.save(savedException);
        }

        return savedException;
    }

    /**
     * Crée une exception spécifique à une WorktimeSeries pour une journée donnée
     * 
     * @param seriesId L'ID de la série
     * @param date     La date pour laquelle créer l'exception
     * @param userId   L'ID de l'utilisateur pour vérification
     * @return L'exception créée
     */
    @Transactional
    public RecurrenceException createWorktimeSeriesException(Long seriesId, LocalDate date, Integer userId) {
        LoggerUtils.info(logger, "Creating worktime series exception for series " + seriesId + " on date " + date);

        // Vérifier que la série existe et appartient à l'utilisateur
        WorktimeSeries series = workTimeSeriesRepository.findById(seriesId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Worktime series not found with id: " + seriesId));

        if (!series.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only create exceptions for your own series.");
        }

        // Vérifier que la série a des horaires définis
        if (series.getStartHour() == null || series.getEndHour() == null) {
            throw new IllegalArgumentException("Cannot create exception for series without defined hours.");
        }

        // Créer les dates de début et fin basées sur les horaires de la série
        LocalTime startTime = series.getStartHour().toLocalTime();
        LocalTime endTime = series.getEndHour().toLocalTime();

        LocalDateTime pauseStart = date.atTime(startTime);
        LocalDateTime pauseEnd = date.atTime(endTime);

        // Vérifier s'il existe déjà une exception pour cette série à cette date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime startOfNextDay = date.plusDays(1).atStartOfDay();
        List<RecurrenceException> existingExceptions = recurrenceExceptionRepository
                .findByExceptionTypeAndTargetSeriesIdAndDateRange(
                        ExceptionType.WORKTIME_SERIES,
                        seriesId,
                        startOfDay,
                        startOfNextDay);

        if (!existingExceptions.isEmpty()) {
            throw new IllegalArgumentException("Une exception existe déjà pour cette série à cette date.");
        }

        // Créer l'exception
        RecurrenceException newException = RecurrenceException.builder()
                .pauseStart(pauseStart)
                .pauseEnd(pauseEnd)
                .exceptionType(ExceptionType.WORKTIME_SERIES)
                .targetSeriesId(seriesId)
                .series(new ArrayList<>())
                .build();

        // Sauvegarder l'exception
        RecurrenceException savedException = recurrenceExceptionRepository.save(newException);

        // Établir la relation bidirectionnelle
        savedException.getSeries().add(series);
        series.getExceptions().add(savedException);

        // Sauvegarder les modifications
        recurrenceExceptionRepository.save(savedException);
        workTimeSeriesRepository.save(series);

        return savedException;
    }

    /**
     * Toggle une exception spécifique à une WorktimeSeries pour une journée donnée
     * Si l'exception existe, elle est supprimée (réactivation)
     * Si l'exception n'existe pas, elle est créée (annulation)
     * 
     * @param seriesId L'ID de la série
     * @param date     La date pour laquelle toggle l'exception
     * @param userId   L'ID de l'utilisateur pour vérification
     * @return L'exception créée si annulation, null si réactivation
     */
    @Transactional
    public RecurrenceException toggleWorktimeSeriesException(Long seriesId, LocalDate date, Integer userId) {
        LoggerUtils.info(logger, "Toggling worktime series exception for series " + seriesId + " on date " + date);

        // Vérifier que la série existe et appartient à l'utilisateur
        WorktimeSeries series = workTimeSeriesRepository.findById(seriesId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Worktime series not found with id: " + seriesId));

        if (!series.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only toggle exceptions for your own series.");
        }

        // Vérifier s'il existe déjà une exception pour cette série à cette date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime startOfNextDay = date.plusDays(1).atStartOfDay();
        List<RecurrenceException> existingExceptions = recurrenceExceptionRepository
                .findByExceptionTypeAndTargetSeriesIdAndDateRange(
                        ExceptionType.WORKTIME_SERIES,
                        seriesId,
                        startOfDay,
                        startOfNextDay);

        if (!existingExceptions.isEmpty()) {
            // Exception existe déjà -> la supprimer (réactiver)
            RecurrenceException exception = existingExceptions.get(0);

            // Retirer l'exception de la série
            series.getExceptions().remove(exception);
            workTimeSeriesRepository.save(series);

            // Supprimer l'exception
            recurrenceExceptionRepository.delete(exception);

            LoggerUtils.info(logger, "Reactivated worktime series " + seriesId + " for date " + date);
            return null; // Indique une réactivation
        } else {
            // Aucune exception -> en créer une (annuler)
            return createWorktimeSeriesException(seriesId, date, userId);
        }
    }

    /**
     * Supprime une exception spécifique à une WorktimeSeries pour une journée
     * donnée
     * 
     * @param seriesId L'ID de la série
     * @param date     La date pour laquelle supprimer l'exception
     * @param userId   L'ID de l'utilisateur pour vérification
     */
    @Transactional
    public void deleteWorktimeSeriesException(Long seriesId, LocalDate date, Integer userId) {
        LoggerUtils.info(logger, "Deleting worktime series exception for series " + seriesId + " on date " + date);

        // Vérifier que la série existe et appartient à l'utilisateur
        WorktimeSeries series = workTimeSeriesRepository.findById(seriesId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Worktime series not found with id: " + seriesId));

        if (!series.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete exceptions for your own series.");
        }

        // Trouver l'exception pour cette série et cette date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime startOfNextDay = date.plusDays(1).atStartOfDay();
        List<RecurrenceException> exceptions = recurrenceExceptionRepository
                .findByExceptionTypeAndTargetSeriesIdAndDateRange(
                        ExceptionType.WORKTIME_SERIES,
                        seriesId,
                        startOfDay,
                        startOfNextDay);

        if (exceptions.isEmpty()) {
            throw new IllegalArgumentException("Aucune exception trouvée pour cette série à cette date.");
        }

        // Supprimer l'exception (il ne devrait y en avoir qu'une)
        RecurrenceException exception = exceptions.get(0);

        // Retirer l'exception de la série
        series.getExceptions().remove(exception);
        workTimeSeriesRepository.save(series);

        // Supprimer l'exception
        recurrenceExceptionRepository.delete(exception);
    }

    /**
     * Vérifie si une série peut être liée à une exception de récurrence
     * 
     * @param seriesId    ID de la série
     * @param exceptionId ID de l'exception
     * @param userId      ID de l'utilisateur pour vérification
     * @return true si la liaison est possible, false sinon
     */
    public boolean canLinkSeriesToException(Integer seriesId, Integer exceptionId, Integer userId) {
        WorktimeSeries series = workTimeSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new RuntimeException("Worktime series not found with id: " + seriesId));

        // Vérifier que l'utilisateur est le propriétaire de la série
        if (!series.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only link exceptions to your own series.");
        }

        // Si la série ignore les exceptions, pas besoin de vérifier plus loin
        if (Boolean.TRUE.equals(series.getIgnoreExceptions())) {
            return false;
        }

        RecurrenceException exception = recurrenceExceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new RuntimeException("Recurrence exception not found with id: " + exceptionId));

        // Vérifier si la série chevauche la période de l'exception
        return series.getStartDate().isBefore(exception.getPauseEnd()) &&
                (series.getEndDate() == null || series.getEndDate().isAfter(exception.getPauseStart()));
    }

    /**
     * Lie une série à une exception de récurrence
     * 
     * @param seriesId    ID de la série
     * @param exceptionId ID de l'exception
     * @param userId      ID de l'utilisateur pour vérification
     */
    @Transactional
    public void linkSeriesToException(Integer seriesId, Integer exceptionId, Integer userId) {
        if (!canLinkSeriesToException(seriesId, exceptionId, userId)) {
            throw new IllegalArgumentException("Cannot link this series to this exception.");
        }

        WorktimeSeries series = workTimeSeriesRepository.findById(seriesId).get();
        RecurrenceException exception = recurrenceExceptionRepository.findById(exceptionId).get();

        series.getExceptions().add(exception);
        exception.getSeries().add(series);

        workTimeSeriesRepository.save(series);
        recurrenceExceptionRepository.save(exception);
    }

    /**
     * Met à jour une exception de récurrence existante
     * 
     * @param id                         L'ID de l'exception à mettre à jour
     * @param recurrenceExceptionDetails Les nouvelles données
     * @param userId                     L'ID de l'utilisateur pour vérification
     *                                   d'autorisation
     * @return L'exception mise à jour
     */
    @Transactional
    public RecurrenceException updateRecurrenceException(Long id, RecurrenceException recurrenceExceptionDetails,
            Integer userId) {
        LoggerUtils.info(logger, "Updating recurrence exception with id: " + id);

        RecurrenceException existingException = recurrenceExceptionRepository.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("Recurrence exception not found with id: " + id));

        // Vérifier que l'utilisateur est le propriétaire d'au moins une des séries
        // associées
        boolean isAuthorized = existingException.getSeries().stream()
                .anyMatch(series -> series.getUser().getId().equals(userId));

        if (!isAuthorized) {
            throw new IllegalArgumentException("You can only update exceptions for your own series.");
        }

        // Vérifier la cohérence des dates
        LocalDateTime start = recurrenceExceptionDetails.getPauseStart() != null
                ? recurrenceExceptionDetails.getPauseStart()
                : existingException.getPauseStart();
        LocalDateTime end = recurrenceExceptionDetails.getPauseEnd() != null ? recurrenceExceptionDetails.getPauseEnd()
                : existingException.getPauseEnd();

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
     * @param id     L'ID de l'exception à supprimer
     * @param userId L'ID de l'utilisateur pour vérification d'autorisation
     */
    @Transactional
    public void deleteRecurrenceException(Long id, Integer userId) {
        LoggerUtils.info(logger, "Deleting recurrence exception with id: " + id);

        RecurrenceException exception = recurrenceExceptionRepository.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("Recurrence exception not found with id: " + id));

        // Vérifier que l'utilisateur est le propriétaire d'au moins une des séries
        // associées
        boolean isAuthorized = exception.getSeries().stream()
                .anyMatch(series -> series.getUser().getId().equals(userId));

        if (!isAuthorized) {
            throw new IllegalArgumentException("You can only delete exceptions for your own series.");
        }

        // Supprimer les références dans les séries
        for (WorktimeSeries series : exception.getSeries()) {
            series.getExceptions().remove(exception);
        }
        workTimeSeriesRepository.saveAll(exception.getSeries());

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
     * @param userId   L'ID de l'utilisateur pour vérification d'autorisation
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

    /**
     * Récupère toutes les exceptions de récurrence pour un utilisateur
     * Exclut les exceptions de type WORKTIME_SERIES
     * 
     * @param userId L'ID de l'utilisateur
     * @return La liste des exceptions de type DAY liées à l'utilisateur et celles
     *         sans séries
     */
    public List<RecurrenceException> getAllRecurrenceExceptionsByUserId(Integer userId) {
        LoggerUtils.info(logger, "Fetching all DAY recurrence exceptions for user: " + userId);
        return recurrenceExceptionRepository.findAllByUserIdOrWithoutSeries(userId).stream()
                .filter(exception -> exception.getExceptionType() == ExceptionType.DAY)
                .toList();
    }
}