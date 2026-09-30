package com.mams.controller;

import com.mams.entity.Assignment;
import com.mams.service.AssignmentService;
import com.mams.repository.AssignmentRepository;
import com.mams.security.AuthUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentRepository assignmentRepository;

    public AssignmentController(AssignmentService assignmentService, AssignmentRepository assignmentRepository) {
        this.assignmentService = assignmentService;
        this.assignmentRepository = assignmentRepository;
    }

    @PostMapping
    public Assignment create(@RequestBody Assignment req) {
        return assignmentService.create(req);
    }

    @PutMapping("/{id}/return")
    public Assignment returnAssignment(@PathVariable Long id) {
        return assignmentService.returnAssignment(id);
    }

    @GetMapping
    public List<Assignment> getAll() {
        List<Assignment> assignments = assignmentRepository.findAll();
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!"ADMIN".equals(user.getRoleName())) {
            assignments = assignments.stream()
                .filter(a -> user.getBaseId().equals(a.getBaseId()))
                .collect(Collectors.toList());
        }
        return assignments;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        assignmentRepository.deleteById(id);
    }
}

