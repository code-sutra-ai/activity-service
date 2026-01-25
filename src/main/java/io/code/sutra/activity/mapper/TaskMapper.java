package io.code.sutra.activity.mapper;

import io.code.sutra.activity.dto.TaskRequest;
import io.code.sutra.activity.entity.Task;

public class TaskMapper {
    public static Task toEntity(TaskRequest req) {
        if (req == null) return null;
        return new Task(req.getId(), req.getTitle(), req.getStatus(), req.getAssignee(), req.getService());
    }
}

