package com.tempo.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tempo.application.model.category.Category;
import com.tempo.application.model.worktime.Worktime;
import com.tempo.application.model.worktime.DTO.WorktimeRequestDTO;
import com.tempo.application.repository.WorktimeRepository;
import com.tempo.application.repository.CategoryRepository;


@Service
public class WorktimeService {
    
    @Autowired
    WorktimeRepository worktimeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public void deleteWorktimeById(int id) {
        if (worktimeRepository.existsById(id)) {
            worktimeRepository.deleteById(id);
        } else {
            throw new RuntimeException("Worktime with id " + id + " does not exist.");
        }
    }

    public void updateWorktime(WorktimeRequestDTO worktimeUpdateRequest) {
        if (worktimeRepository.existsById(worktimeUpdateRequest.getId())) {
            Worktime worktime = worktimeRepository.findById(worktimeUpdateRequest.getId())
                .orElseThrow(() -> new RuntimeException("Worktime not found"));
                
            // Get the Category object using the ID from the request
            Category category = categoryRepository.findById(worktimeUpdateRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
                
            worktime.setStartTime(worktimeUpdateRequest.getStartTime());
            worktime.setEndTime(worktimeUpdateRequest.getEndTime());
            worktime.setCategory(category); // Pass the Category object, not the ID
            worktimeRepository.save(worktime);
        } else {
            throw new RuntimeException("Worktime with id " + worktimeUpdateRequest.getId() + " does not exist.");
        }
    }

}
