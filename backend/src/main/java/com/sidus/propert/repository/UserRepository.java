package com.sidus.propert.repository;

import java.util.Optional;

import com.sidus.propert.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
}