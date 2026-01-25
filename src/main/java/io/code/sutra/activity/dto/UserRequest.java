package io.code.sutra.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {
    private Long id;

    @NotBlank(message = "name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String phone;

    @Size(max = 2000)
    private String notes;
}

