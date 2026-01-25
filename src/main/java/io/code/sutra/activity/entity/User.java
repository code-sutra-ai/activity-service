package io.code.sutra.activity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    // accept non-nullable id provided by code (DataInitializer, IdGenerator)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    // Optional fields
    @Column(nullable = true)
    private String email;

    @Column(nullable = true)
    private String phone;

    @Column(nullable = true, length = 2000)
    private String notes;

    // Keep a convenience constructor used by tests and code that supplies id and name only
    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        if (this.name == null || this.name.isEmpty()) return this.name;
        String original = this.name;
        // Capitalize the first character and concatenate with the rest of the string
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}