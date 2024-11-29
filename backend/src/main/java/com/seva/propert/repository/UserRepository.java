package com.seva.propert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import com.seva.propert.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}