package com.hackethon.app.kafka;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.hackethon.app.dto.TransactionEvent;
import com.hackethon.app.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FraudDetectionConsumer {

    private final UserService userService;

    // In Memory Sliding Window Storage:: Sender/Source Account -> Last 1 Min
    private final Map<String, List<TransactionEvent>> memoryWindowCache = new ConcurrentHashMap<>();

    public void consumeTransactionStream(TransactionEvent event) {
        System.out.println("Kafka Event Received -> Sender: " + event.getSourceAccountNumber()
                + " | Receiver: " + event.getTargetAccountNumber()
                + " | Amt: " + event.getAmount()
                + " | Loc: " + event.getLocation());

        String sourceAcc = event.getSourceAccountNumber();
        long now = System.currentTimeMillis();

        /*
         * -----------------------------------------------------------------
         * RULE 1: Check High Amount(Before Deducting)
         * -----------------------------------------------------------------
         */
        if (event.getAmount() >= 300000) {
            System.out.println("RULE 1 TRIGGERED: Amount >= 3 Lakh! Suspending Sender Account: " + sourceAcc);
            userService.suspendAccountDueToFraud(sourceAcc);
            return;
        }

        /*
         * -----------------------------------------------------------------
         * RULE 2: Sliding Window Multi-Location Check (1 Minute Window)
         * -----------------------------------------------------------------
         */
        List<TransactionEvent> txnHistory = memoryWindowCache.computeIfAbsent(sourceAcc,
                k -> new CopyOnWriteArrayList<>());
        txnHistory.add(event);

        // Cleaning Engine: Delete transactions those are old than 60000 MS (60
        // Seconds);
        txnHistory.removeIf(txn -> (now - txn.getTimestamp()) > 60000);

        // Count Distinct Locations for this sender in the last 1 minute
        long uniqueLocationCount = txnHistory.stream().map(TransactionEvent::getLocation).distinct().count();

        if (uniqueLocationCount > 1) {
            System.out.println("RULE 2 TRIGGERED: Multi-Location velocity anomaly in 1 Min! Suspending Sender Account: "
                    + sourceAcc);
            userService.suspendAccountDueToFraud(sourceAcc);
            return;
        }

        /*
         * -----------------------------------------------------------------------------
         * SETTLEMENT STAGE: If both check is passed then move forward towards
         * credit/debit;
         * -----------------------------------------------------------------------------
         */
        try {
            userService.processSuccessfulTransaction(
                    event.getSourceAccountNumber(),
                    event.getTargetAccountNumber(),
                    event.getTransactionId(),
                    event.getLocation(),
                    event.getAmount());
            System.out.println(
                    "Transaction settled successfully from " + sourceAcc + " to " + event.getTargetAccountNumber());
        } catch (Exception e) {
            System.out.println("Settlement Failed: " + e.getMessage());
        }

    }

}
