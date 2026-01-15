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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
        String original = this.assignee;
        // Capitalize the first character and concatenate with the rest of the string
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}