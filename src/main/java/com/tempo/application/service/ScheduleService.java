package com.tempo.application.service;

import com.tempo.application.model.schedule.ScheduleDateEntryDTO;
import com.tempo.application.model.schedule.ScheduleEntryDTO;
import com.tempo.application.model.schedule.ScheduleThreeDaysDTO;
import com.tempo.application.model.worktime.Worktime;
import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tempo.application.utils.LoggerUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private static final Logger logger = LoggerUtils.getLogger(ScheduleService.class);

    @Autowired
    private WorktimeService worktimeService;

    @Autowired
    private WorktimeSeriesService worktimeSeriesService;

    /**
     * Récupère toutes les entrées de planification (Worktime et WorktimeSeries) pour une date donnée et un utilisateur
     *
     * @param date La date pour laquelle récupérer les entrées
     * @param userId L'ID de l'utilisateur
     * @return Une liste combinée des entrées de planification
     */
    public List<ScheduleEntryDTO> getUserScheduleByDate(LocalDate date, Integer userId) {
        LoggerUtils.info(logger, "Fetching user schedule for date: " + date + " and user id: " + userId);
        
        List<Worktime> worktimes = worktimeService.getAllUserWorktimesByDateAndUserId(date, userId);
        
        List<ScheduleEntryDTO> schedule = new ArrayList<>();
        
        // Convertir les worktimes en DTOs
        List<ScheduleEntryDTO> worktimeDTOs = worktimes.stream()
            .filter(worktime -> worktime.getEndHour() != null) // Exclure les chronos en cours pour éviter la duplication
            .map(ScheduleEntryDTO::fromWorktime)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
        
        schedule.addAll(worktimeDTOs);
        
        // Récupérer les séries récurrentes actives pour cette date
        List<WorktimeSeries> activeSeries = worktimeSeriesService.getActiveWorkTimeSeriesForDateAndUser(date, userId);
        
        // Filtrer les séries par jour de la semaine
        List<WorktimeSeries> filteredSeries = filterSeriesByDayOfWeek(activeSeries, date);
        
        // Convertir les séries filtrées en DTOs
        List<ScheduleEntryDTO> workTimeSeriesDTOs = filteredSeries.stream()
            .map(ScheduleEntryDTO::fromWorkTimeSeries)
            .collect(Collectors.toList());
        
        schedule.addAll(workTimeSeriesDTOs);
        
        // Récupérer les créneaux en cours (endTime = null)
        List<Worktime> ongoingWorktimes = worktimeService.getOngoingWorktimesByUserId(userId);
        List<ScheduleEntryDTO> ongoingWorktimeDTOs = ongoingWorktimes.stream()
            .map(worktime -> ScheduleEntryDTO.builder()
                        .id((long) worktime.getId())
                        .type("CHRONO")
                        .startHour(worktime.getStartHour())
                        .categoryId((long) worktime.getCategory().getId())
                        .categoryName(worktime.getCategory().getName())
                        .build())
            .collect(Collectors.toList());
        
        schedule.addAll(ongoingWorktimeDTOs);
        
        // Trier par heure de début
        return schedule.stream()
            .sorted(Comparator.comparing(
                entry -> entry.getStartHour() != null ? entry.getStartHour().toLocalTime() : null,
                Comparator.nullsLast(Comparator.naturalOrder())
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Filtre une liste de séries récurrentes pour ne conserver que celles qui sont actives
     * pour le jour de la semaine spécifié dans la date
     * 
     * @param series La liste des séries à filtrer
     * @param date La date pour laquelle vérifier l'activité
     * @return Une liste filtrée de séries actives pour ce jour
     */
    private List<WorktimeSeries> filterSeriesByDayOfWeek(List<WorktimeSeries> series, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String dayCode = getDayCode(dayOfWeek);
        
        return series.stream()
            .filter(s -> {
                String recurrenceRule = s.getRecurrence();
                if (recurrenceRule == null || recurrenceRule.isEmpty()) {
                    return false;
                }
                if (recurrenceRule.contains("BYDAY=")) {
                    String byday = recurrenceRule.substring(recurrenceRule.indexOf("BYDAY=") + 6);
                    if (byday.contains(";")) {
                        byday = byday.substring(0, byday.indexOf(";"));
                    }
                    return byday.contains(dayCode);
                }
                return true;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Convertit un DayOfWeek Java en code de jour RFC5545 (MO, TU, WE, etc.)
     */
    private String getDayCode(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "MO";
            case TUESDAY: return "TU";
            case WEDNESDAY: return "WE";
            case THURSDAY: return "TH";
            case FRIDAY: return "FR";
            case SATURDAY: return "SA";
            case SUNDAY: return "SU";
            default: return "";
        }
    }

    /**
     * Récupère toutes les entrées de planification (Worktime et WorktimeSeries) pour un mois donné et un utilisateur
     *
     * @param date Une date du mois pour lequel récupérer les entrées
     * @param userId L'ID de l'utilisateur
     * @return Une liste combinée des entrées de planification du mois
     */
    public List<ScheduleDateEntryDTO> getUserScheduleByMonth(LocalDate date, Integer userId) {
        LoggerUtils.info(logger, "Getting schedule for month: " + date.getMonth() + " and user id: " + userId);
        List<ScheduleDateEntryDTO> schedule = new ArrayList<>();

        // Récupérer les Worktime pour ce mois
        List<Worktime> worktimes = worktimeService.getAllUserWorktimesByMonthAndUserId(date, userId);
        LoggerUtils.info(logger, "Found " + worktimes.size() + " worktime entries for the month");
        
        List<ScheduleDateEntryDTO> worktimeDTOs = worktimes.stream()
                .map(ScheduleDateEntryDTO::fromWorktime)
                .collect(Collectors.toList());
        schedule.addAll(worktimeDTOs);

        // Récupérer les WorktimeSeries actives pour ce mois
        List<WorktimeSeries> activeSeries = worktimeSeriesService.getActiveWorkTimeSeriesForMonthAndUser(date, userId);
        LoggerUtils.info(logger, "Found " + activeSeries.size() + " worktime series entries for the month");
        
        // Pour les séries mensuelles, nous n'avons pas besoin de filtrer par jour de la semaine
        // car nous voulons toutes les séries actives durant le mois
        List<ScheduleDateEntryDTO> workTimeSeriesDTOs = activeSeries.stream()
                .map(ScheduleDateEntryDTO::fromWorktimeSeries)
                .collect(Collectors.toList());
        schedule.addAll(workTimeSeriesDTOs);

        return schedule;
    }

    public ScheduleThreeDaysDTO getUserScheduleForThreeDays(LocalDate date, Integer userId) {
        LoggerUtils.info(logger, "Getting schedule for three days around: " + date + " for user id: " + userId);
        
        return ScheduleThreeDaysDTO.builder()
            .yesterday(getUserScheduleByDate(date.minusDays(1), userId))
            .today(getUserScheduleByDate(date, userId))
            .tomorrow(getUserScheduleByDate(date.plusDays(1), userId))
            .build();
    }
}