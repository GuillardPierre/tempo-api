package com.tempo.application.controller;

import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/worktimes")
public class WorktimeSeriesController {

    @PostMapping("/create")
    public ResponseEntity<?> createWorktimeSeries(@Valid @RequestBody WorktimeSeries worktimeSeriesRequest) {
        return new ResponseEntity<>(worktimeSeriesRequest, HttpStatus.CREATED);
    }
}
