package io.code.sutra.activity.mapper;

import io.code.sutra.activity.dto.UserRequest;
import io.code.sutra.activity.entity.User;

public class UserMapper {
    public static User toEntity(UserRequest req) {
        if (req == null) return null;
        return new User(req.getId(), req.getName(), req.getEmail(), req.getPhone(), req.getNotes());
    }
}

