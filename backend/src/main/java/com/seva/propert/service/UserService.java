package com.seva.propert.service;

import java.util.Optional;
import com.seva.propert.model.entity.User;

public interface UserService {
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    User create(User user);
}
