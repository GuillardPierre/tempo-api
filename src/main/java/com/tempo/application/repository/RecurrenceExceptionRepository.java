package com.tempo.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tempo.application.model.recurrenceException.RecurrenceException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecurrenceExceptionRepository extends JpaRepository<RecurrenceException, Integer> {
    
    /**
     * Trouve une exception exactement pour une période donnée
     */
    Optional<RecurrenceException> findByPauseStartAndPauseEnd(LocalDateTime pauseStart, LocalDateTime pauseEnd);

    /**
     * Trouve toutes les exceptions qui se chevauchent avec la période donnée.
     * Une exception chevauche si elle partage au moins un jour avec la période donnée.
     */
    @Query("SELECT re FROM RecurrenceException re " +
           "WHERE (re.pauseStart < :endDate AND re.pauseEnd > :startDate)")
    List<RecurrenceException> findOverlappingExceptions(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Trouve toutes les exceptions liées à un utilisateur donné
     */
    @Query("SELECT DISTINCT re FROM RecurrenceException re " +
           "LEFT JOIN re.series s " +
           "LEFT JOIN s.user u " +
           "WHERE u.id = :userId OR " +
           "(re.series IS EMPTY)")  // Inclure aussi les exceptions sans séries
    List<RecurrenceException> findAllByUserIdOrWithoutSeries(@Param("userId") Integer userId);

}
