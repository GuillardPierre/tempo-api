package com.tempo.application.model.worktimeSeries;

import java.time.LocalDateTime;
import java.util.List;

import com.tempo.application.model.category.Category;
import com.tempo.application.model.recurrenceException.RecurrenceException;
import com.tempo.application.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Représente une série récurrente de créneaux.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorktimeSeries {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Category category;

    @NotBlank
    private LocalDateTime startDate;
    private LocalDateTime endDate;    // nullable = récursif jusqu'à annulation

    @Column(nullable = false)
    private String rrule;            // RFC5545, ex. "FREQ=WEEKLY;BYDAY=MO,WE,FR"

    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecurrenceException> exceptions;
}