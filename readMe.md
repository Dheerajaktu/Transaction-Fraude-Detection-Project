# Real-Time Transaction Processing & Fraud Detection System
## End-to-End Application Flow & Architecture Document

This document outlines the end-to-end flow, component architecture, data models, and business logic for the Event-Driven Banking MVP developed for the **HCLTech / Lloyds Banking Group Hackathon**.

---

## 1. High-Level Architecture Overview

The system uses an **Event-Driven Architecture (EDA)** built with Java 17, Spring Boot, Apache Kafka, and a MySQL relational database. The core focus is maintaining high-throughput transaction streaming while running low-latency, in-memory fraud detection before mutating states inside the database management system.

```
                  +--------------------------+
                  |    Postman / Client      |
                  +-------------+------------+
                                |
                                | (1) POST /v1/transactions/initiate (JSON)
                                v
                  +--------------------------+
                  |  TransactionProducer     |
                  |        Controller        |
                  +-------------+------------+
                                |
                                | (2) kafkaTemplate.send(TOPIC, event)
                                v
                  +--------------------------+
                  |      Apache Kafka        |
                  |   [transactions-topic]   |
                  +-------------+------------+
                                |
                                | (3) Asynchronous Stream Consume
                                v
                  +--------------------------+
                  |  FraudDetectionConsumer  |  <--- [In-Memory Sliding Cache Map]
                  +-------------+------------+
                                |
             +------------------+------------------+
             | (4) Pass                            | (4) Fail (Fraud Detected)
             v                                     v
+----------------------------+       +----------------------------+
|  userService.              |       |  userService.              |
|  processSuccessfulTx()     |       |  suspendAccountDueToFraud()|
+------------+---------------+       +------------+---------------+
             |                                     |
             +------------------+------------------+
                                | (5) Database State Mutation
                                v
                  +--------------------------+
                  |    MySQL Database        |
                  |    [bank_users Table]    |
                  +--------------------------+
```

---

## 2. Step-by-Step Data Flow Pipeline

### Step 1: Ingestion Layer (Client to Inbound REST Layer)
* **Action:** The client triggers a financial transaction by hitting the endpoint `POST /v1/transactions/initiate` via Postman.
* **Payload Structuring:** The system captures critical entities: `transactionId`, `sourceAccountNumber` (Sender), `targetAccountNumber` (Receiver), `amount`, and `location`.
* **Asynchronous Offloading:** If the payload doesn't contain a timestamp, the framework attaches `System.currentTimeMillis()`. The controller immediately submits this transaction event record into the Kafka queue broker. It safely offloads the operations and hands over a generic HTTP `200 OK` back to the mobile client, making the network layer fully non-blocking.

### Step 2: Streaming Layer (Apache Kafka Broker Pipeline)
* **Message Ingestion:** The message logs successfully land inside the designated cluster structure partition of **`transactions-topic`**.
* **Sequence Ordering Assurance:** The system utilizes the user's explicit unique account ID or string format value as the explicit routing Kafka Message Key. This guarantees that all chronological records sent by a single specific bank account arrive inside the exact same message broker partition in sequential order.

### Step 3: Real-Time Complex Event Processing Layer (Fraud Consumer Engine)
* **Continuous Stream Monitoring:** The component active framework flags message ingestion alerts inside the target topic stream via the decoupled execution container marked with `@KafkaListener`.
* **State Assessment Checkpoints:** The background stream evaluator processes incoming entries directly over **Java RAM Space** through a high-performance concurrent structure (`ConcurrentHashMap` bound to thread-safe collections) acting as a sliding memory temporal cache. It processes two key guardrail verification rules:
  * **Rule Engine Check A (Stateless Verification):** If `event.getAmount() > 500000`, it declares an immediate breach anomaly without evaluating time windows.
  * **Rule Engine Check B (Stateful Sliding Time-Window Analytics):** The application adds the transaction metadata to the account memory collection and instantly invokes a declarative cleanup wrapper: `txHistory.removeIf(tx -> (now - tx.getTimestamp()) > 60000);`. Any event structural state log recorded over **60 seconds ago** is deleted. If the remaining unique element set size of `.distinct().count()` over geographic location dimensions yields a value strictly greater than 1, a concurrent velocity anomaly fraud flag is raised.

### Step 4: Transaction Isolation & Final Settlement Layer (MySQL Target Storage)
* **Execution Path A (Fraud Flag Interruption):** The system breaks further downstream updates to the standard payment pipelines, avoiding arbitrary balances deductions. It redirects calls towards `userService.suspendAccountDueToFraud()`, altering the entity row state property mapping flag column value to `"SUSPENDED_FRAUD"` within database transactional constraints.
* **Execution Path B (Verified Settlement Pipeline):** When the transaction passes the rules cleanly, control flows into `userService.processSuccessfulTransaction()`. Supported by the explicit container propagation proxy metadata tag **`@Transactional`**, it loads and guarantees ACID-compliant account state transitions:
  1. The engine checks if the sender's current asset balance safely satisfies the target debit requirements.
  2. The database updates the ledger entries: Subtracts the balance from the sender's record and increments the target receiver's record with the exact matching credits.
  3. It overwrites tracking fields (`lastTransactionLocation`, `lastTransactionAmount`, `lastTransactionId`) directly on the single-entity record row, providing a fast audit log without additional join tables.

---

## 3. Core Structural Data Schemas

### Request / Message Transport Blueprint (JSON Payload DTO)
```json
{
    "transactionId": "TXN-LBG-8849",
    "sourceAccountNumber": "LLOYDS-SENDER-001",
    "targetAccountNumber": "LLOYDS-RECEIVER-002",
    "amount": 25000.00,
    "location": "Noida",
    "timestamp": 0
}
```

### DBMS Model Engine Representation (The Single Hackathon Entity Schema)
```java
@Entity
@Table(name = "bank_users")
@Data
@NoArgsConstructor
public class UsersEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(unique = true, nullable = false)
    private String accountNumber;
    
    private float balance;
    
    // Low-overhead Audit State Matrix tracking parameters
    private String lastTransactionId;
    private String lastTransactionLocation;
    private float lastTransactionAmount;
    private String accountStatus = "ACTIVE"; // ACTIVE or SUSPENDED_FRAUD
    
    private LocalDateTime transactionTime = LocalDateTime.now();
}
```

---
## 4. Key Engineering Trade-offs & Rationale
1. **Low-Latency Sliding Window Strategy:** By processing the 1-minute geographic velocity window using an in-memory `ConcurrentHashMap` clean-up structure instead of heavy native DB SQL aggregates (`SELECT COUNT(DISTINCT...) WHERE timestamp > NOW() - 1 MIN`), database execution overhead drops close to zero.
2. **Atomic Rollback Reliability:** The use of `@Transactional` guarantees strict atomic compliance. If the server drops or network limits break right after debiting the sender but right before updating the receiver, the database engine enforces a full transactional data state rollback.
