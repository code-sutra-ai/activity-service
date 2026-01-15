package io.code.sutra.activity.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class UserDTO {
    private Long id;
    private String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CreateUserDTO {
    private String name;
}
