package io.code.sutra.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String title;
    private String status;
    private String assignee;
    private String service;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CreateTaskDTO {
    private String title;
    private String assignee;
    private String status;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class UpdateTaskDTO {
    private String title;
    private String status;
    private String assignee;
}