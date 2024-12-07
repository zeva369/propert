package com.seva.propert.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.seva.propert.model.entity.User;
import com.seva.propert.repository.UserRepository;
import com.seva.propert.service.UserService;

@Service
public class UserServiceImpl implements UserService{
    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo){
        this.repo = repo;
    }   
    
    @Override
    public Optional<User> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }

     // Método para guardar un usuario
     @Override
     public User create(User user) {
        return repo.save(user);
    }

    
}
