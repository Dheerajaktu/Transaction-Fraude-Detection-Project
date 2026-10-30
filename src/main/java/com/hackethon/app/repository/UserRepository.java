package com.hackethon.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackethon.app.entity.UsersEntity;

public interface UserRepository extends JpaRepository<UsersEntity, Long>{

    Optional<UsersEntity> findByAccountNumber(String accountNumber);
    
    boolean existsByAccountNumber(String accountNumber);
} 