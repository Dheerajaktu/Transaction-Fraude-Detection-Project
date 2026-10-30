package com.hackethon.app.service;

import com.hackethon.app.entity.UsersEntity;



public interface UserService {
    UsersEntity createNewUserAcount(UsersEntity user);

    UsersEntity getAccountByNumber(String accountNumber);

    void processSuccessfulTransaction(String sourceAccount, String targetAccount, String txId, String location, float amount);

    void suspendAccountDueToFraud(String accountNumber);
}