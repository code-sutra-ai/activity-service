package io.code.sutra.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.Data;

@Entity
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class Task {
    @Id
    // Accept externally provided IDs (DataInitializer uses explicit ids)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String status; // pending, in-progress, completed

    private String assignee;

    @Column(nullable = false)
    private String service;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAssignee() {
        if (this.assignee == null || this.assignee.isEmpty()) return this.assignee;
        String original = this.assignee;
        // Capitalize the first character and concatenate with the rest of the string
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}