package com.tempo.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tempo.application.model.user.User;
import com.tempo.application.model.worktime.Worktime;
import com.tempo.application.model.category.Category;
import com.tempo.application.model.worktime.DTO.CategoryStatDTO;

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

    void deleteByCategory(Category category);

    @Query("SELECT new com.tempo.application.model.worktime.DTO.CategoryStatDTO(" +
           "w.category.name, " +
           "CAST(SUM(FUNCTION('TIMESTAMPDIFF', MINUTE, w.startTime, w.endTime)) AS int)) " +
           "FROM Worktime w " +
           "WHERE w.user.id = :userId " +
           "AND w.startTime >= :from " +
           "AND w.endTime <= :to " +
           "AND w.endTime IS NOT NULL " +
           "GROUP BY w.category.name")
    List<CategoryStatDTO> getCategoryStatsByUserAndPeriod(@Param("userId") Integer userId,
                                                          @Param("from") LocalDateTime from,
                                                          @Param("to") LocalDateTime to);
                                                          
}
