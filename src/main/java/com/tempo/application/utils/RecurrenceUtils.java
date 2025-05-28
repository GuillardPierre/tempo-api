package com.tempo.application.utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecurrenceUtils {
    /**
     * Génère les occurrences pour une règle de type FREQ=WEEKLY;BYDAY=MO,WE,FR
     * @param rrule exemple : "FREQ=WEEKLY;BYDAY=MO,WE,FR"
     * @param start date de début de la série
     * @param from borne de début de la période à générer
     * @param to borne de fin de la période à générer
     * @return liste des LocalDateTime correspondant aux occurrences
     */
    public static List<LocalDateTime> generateOccurrences(
            String rrule,
            LocalDateTime start,
            LocalDateTime from,
            LocalDateTime to) {
        List<LocalDateTime> occurrences = new ArrayList<>();
        if (rrule == null || !rrule.startsWith("FREQ=WEEKLY")) {
            // On ne gère que les règles hebdomadaires simples
            return occurrences;
        }
        // Extraire les jours de la semaine
        Map<String, DayOfWeek> dayMap = new HashMap<>();
        dayMap.put("MO", DayOfWeek.MONDAY);
        dayMap.put("TU", DayOfWeek.TUESDAY);
        dayMap.put("WE", DayOfWeek.WEDNESDAY);
        dayMap.put("TH", DayOfWeek.THURSDAY);
        dayMap.put("FR", DayOfWeek.FRIDAY);
        dayMap.put("SA", DayOfWeek.SATURDAY);
        dayMap.put("SU", DayOfWeek.SUNDAY);

        List<DayOfWeek> daysOfWeek = new ArrayList<>();
        String[] parts = rrule.split(";");
        for (String part : parts) {
            if (part.startsWith("BYDAY=")) {
                String[] days = part.substring(6).split(",");
                for (String d : days) {
                    if (dayMap.containsKey(d)) {
                        daysOfWeek.add(dayMap.get(d));
                    }
                }
            }
        }
        if (daysOfWeek.isEmpty()) {
            // Si aucun jour n'est précisé, on ne génère rien
            return occurrences;
        }
        // Générer les occurrences
        LocalDateTime current = start.isBefore(from) ? from : start;
        // On commence à la première semaine qui intersecte la période
        while (!current.isAfter(to)) {
            if (!current.isBefore(start) && daysOfWeek.contains(current.getDayOfWeek())) {
                occurrences.add(current.withHour(start.getHour()).withMinute(start.getMinute()).withSecond(0).withNano(0));
            }
            current = current.plusDays(1);
        }
        return occurrences;
    }
} 