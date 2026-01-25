package io.code.sutra.activity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskRequest {
    private Long id;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "status is required")
    private String status;

    private String assignee;

    @NotBlank(message = "service is required")
    private String service;

    private String description;
}

