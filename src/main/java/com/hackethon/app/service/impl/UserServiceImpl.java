package com.hackethon.app.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.hackethon.app.entity.UsersEntity;
import com.hackethon.app.repository.UserRepository;
import com.hackethon.app.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UsersEntity createNewUserAcount(UsersEntity user) {
        return userRepository.save(user);
    }

    @Override
    public UsersEntity getAccountByNumber(String accountNumber) {
        return userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found for: " + accountNumber));
    }

    @Override
    @Transactional
    public void processSuccessfulTransaction(String sourceAccount, String targetAccount, String txId, String location,
            float amount) {
        UsersEntity sender = getAccountByNumber(sourceAccount);
        UsersEntity receiver = getAccountByNumber(targetAccount);

        // Check Sender's balance availability
        if (sender.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance for account: " + sourceAccount);
        }

        // --- DEBIT FROM SENDER (User A) ---
        sender.setBalance(sender.getBalance() - amount);
        sender.setLastTransactionId(txId);
        sender.setLastTransactionLocation(location);
        sender.setLastTransactionAmount(amount);
        sender.setTransactionTime(LocalDateTime.now());
        userRepository.save(sender);

        // --- CREDIT TO RECEIVER (User B) ---
        receiver.setBalance(receiver.getBalance() + amount);
        receiver.setTransactionTime(LocalDateTime.now());
        userRepository.save(receiver);
    }

    @Override
    @Transactional
    public void suspendAccountDueToFraud(String accountNumber) {
        UsersEntity user = getAccountByNumber(accountNumber);
        user.setAccountStatus("SUSPENDED_FRAUD");
        user.setTransactionTime(LocalDateTime.now());

        userRepository.save(user);
    }

}
