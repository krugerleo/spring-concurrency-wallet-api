package com.walletapi.transaction;

import com.walletapi.wallet.Wallet;
import com.walletapi.wallet.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TransactionIdempotencyTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletRepository walletRepository;

    private Long sourceWalletId;
    private Long destinationWalletId;
    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();

        Wallet source = new Wallet();
        source.setBalance(new BigDecimal("1000.00"));
        source = walletRepository.save(source);
        sourceWalletId = source.getId();

        Wallet destination = new Wallet();
        destination.setBalance(new BigDecimal("1000.00"));
        destination = walletRepository.save(destination);
        destinationWalletId = destination.getId();
    }

    @Test
    void testDuplicateRequestWithSameIdempotencyKeyShouldNotDebitTwice() {
        String sameKey = "fixed-idempotency-key-12345";
        BigDecimal transferAmount = new BigDecimal("10.00");

        TransactionRequestDTO request = new TransactionRequestDTO(
                sourceWalletId, destinationWalletId, transferAmount, sameKey
        );

        // 1ª Chamada: Processa normalmente
        Transaction firstTransaction = transactionService.transfer(request);

        // 2ª Chamada: Reenviada com a MESMA chave (replay/retry)
        Transaction secondTransaction = transactionService.transfer(request);

        // Validações de Idempotência
        assertNotNull(firstTransaction.getId());
        assertNotNull(secondTransaction.getId());
        assertEquals(firstTransaction.getId(), secondTransaction.getId(), "Deve retornar o mesmo ID de transação");

        Wallet sourceAfter = walletRepository.findById(sourceWalletId).orElseThrow();
        Wallet destinationAfter = walletRepository.findById(destinationWalletId).orElseThrow();

        // O saldo deve ter sido alterado APENAS para 1 transferência (1000 - 10 = 990)
        assertEquals(0, new BigDecimal("990.00").compareTo(sourceAfter.getBalance()));
        assertEquals(0, new BigDecimal("1010.00").compareTo(destinationAfter.getBalance()));
    }
}