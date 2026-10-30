package com.hackethon.app.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bank_users") // 
@Getter 
@Setter 
@Builder 
@NoArgsConstructor
@AllArgsConstructor
public class UsersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String mobile;

    @Column(unique = true, nullable = false)
    private String accountNumber;
    private float balance;

    // current Transaction Detials
    private String transactionId;
    private String transactionLocation;
    private String transactionAmount;
    private LocalDateTime transactionTime;

    // Last Transaction Details
    private String lastTransactionId;
    private String lastTransactionLocation;
    private float lastTransactionAmount;
    private LocalDateTime lastTransactionTime;

    private String accountStatus = "ACTIVE";
    private LocalDateTime userCreationTime = LocalDateTime.now();
}