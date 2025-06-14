package com.tempo.application.controller;

import com.tempo.application.model.worktime.DTO.CategoryStatDTO;
import com.tempo.application.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.tempo.application.model.user.User;
import com.tempo.application.repository.UserRepository;


@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public Object getStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "week") String type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        List<CategoryStatDTO> categories = statsService.getCategoryStats(user.getId(), from, to);
        Object total = statsService.getTotalWorkTime(user.getId(), from, to, type);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("categories", categories);
        result.put("total", total);
        return result;
    }
}  