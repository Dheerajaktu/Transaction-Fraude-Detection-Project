package com.hackethon.app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackethon.app.entity.UsersEntity;
import com.hackethon.app.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/v1/account")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping("/new")
    public ResponseEntity<UsersEntity> createNewAccount(@RequestBody UsersEntity user) {
        UsersEntity createdUser = service.createNewUserAcount(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<UsersEntity> getAccountDetails(@PathVariable String accountNumber) {
        UsersEntity user = service.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(user);
    }


    

}
