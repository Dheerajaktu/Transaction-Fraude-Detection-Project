package com.hackethon.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackethon.app.dto.TransactionEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
public class TransactionProducerController {

    private final KafkaTemplate<String, TransactionEvent> kafkaTempalte;
    private static final String TOPIC = "transactions-topic";

    @PostMapping("/initiate")
    public ResponseEntity<String> initiateTransaction(@RequestBody TransactionEvent event) {

        if (event.getTimestamp() == 0) {
            event.setTimestamp(System.currentTimeMillis());
        }

        // Message sent to Apache Kafka Broker
        kafkaTempalte.send(TOPIC, event);
        return ResponseEntity.ok("Transaction event published successfully to Kafka!");
    }

}
