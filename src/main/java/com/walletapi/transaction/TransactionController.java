package com.walletapi.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    public ResponseEntity<Transaction> transfer(@Valid @RequestBody TransactionRequestDTO request) {
        Transaction transaction = transactionService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}
