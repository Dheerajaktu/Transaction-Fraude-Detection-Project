package com.hackethon.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {

    private String transactionId;
    private String sourceAccountNumber; // User A
    private String targetAccountNumber; // User B
    private float amount;
    private String location;
    private long timestamp;

}
