# Financial Transaction API — Concurrency & Idempotency

A Spring Boot proof of concept demonstrating database-level concurrency control (**Pessimistic Locking**) and duplicate request handling (**Idempotency Keys**).

## Quick Start

### Prerequisites
* Java 21+
* Maven 3.8+

### Run Tests
To execute the multi-threaded concurrency test and idempotency suite:

```bash
mvn test
```

### Run Application
```bash
mvn spring-boot:run
```

## Core Problems & Solutions

### 1. Lost Updates (Pessimistic Locking)
Simultaneous debit requests against the same wallet can lead to race conditions where one transaction overwrites another.

* **Implementation:** `WalletRepository.findByIdWithLock` uses `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
* **Database Behavior:** Issues `SELECT ... FOR UPDATE`, acquiring a row-level write lock that serializes concurrent access at the database level.

### 2. Duplicate Payments (Idempotency Key)
Client retries or network failures can cause duplicate charges for the same transaction.

* **Implementation:** `TransactionService` validates the `Idempotency-Key` header against a `UNIQUE` database index before processing funds.
* **Database Behavior:** If a matching key exists, the service returns the existing transaction record immediately without modifying balances.

---

## Architecture

Package-by-feature layout separating domain contexts:

```text
src/main/java/com/walletapi/
├── config/           # Global exception handlers
├── transaction/      # Transaction entity, DTOs, service, controller
├── user/             # User domain entity
└── wallet/           # Wallet domain entity and pessimistic locking repository
```

---

## Testing Strategy

The test suite validates both edge cases:

1. **`TransactionServiceConcurrencyTest`**: Spawns 10 parallel threads via `ExecutorService` and synchronizes completion using `CountDownLatch`. Verifies that 10 concurrent $10.00 transfers against a $1000.00 initial balance result in a final balance of exactly $900.00.
2. **`TransactionIdempotencyTest`**: Sends back-to-back requests sharing the same `Idempotency-Key` and asserts that only a single debit operation occurs.

---

## Design Trade-Offs

| Approach | Advantage | Disadvantage | Selection |
| :--- | :--- | :--- | :--- |
| **Pessimistic Lock (`FOR UPDATE`)** | Guarantees consistency; no client-side retry logic needed. | Increases latency under heavy row contention. | **Selected** — Preferred for account balance debits. |
| **Optimistic Lock (`@Version`)** | High read throughput; no database row locks held. | Throws exceptions on conflict; requires retry loops. | Alternative for low-write contention. |