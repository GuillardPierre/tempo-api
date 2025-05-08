package com.tempo.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tempo.application.model.user.User;
import com.tempo.application.model.worktime.Worktime;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorktimeRepository extends JpaRepository<Worktime, Integer> {
    Optional<Worktime> findById(int id);

    // Retourne tous les worktimes liés à un id utilisateur
    List<Worktime> findByUserId(Integer userId);

    // Retourne tous les worktimes pour une plage de dates
    List<Worktime> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    
    // Retourne tous les worktimes pour une plage de dates et un utilisateur spécifique
    List<Worktime> findByStartTimeBetweenAndUser(LocalDateTime start, LocalDateTime end, User user);

    // Retourne tous les worktimes actifs pour un utilisateur
    List<Worktime> findByUserIdAndActiveTrueAndEndTimeIsNull(Integer userId);
}
