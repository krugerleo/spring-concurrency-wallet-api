package com.walletapi.transaction;

import com.walletapi.wallet.Wallet;
import com.walletapi.wallet.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.rmi.server.UID;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class TransactionServiceConcurrencyTest {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Long sourceWalletId;
    private Long destinationWalletId;

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
    void testConcurrentTransfers() throws InterruptedException {
        int numberOfThreads = 10;
        BigDecimal transferAmount = new BigDecimal("10.00");

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    String uniqueKey = UUID.randomUUID().toString();
                    transactionService.transfer(
                            new TransactionRequestDTO(sourceWalletId, destinationWalletId, transferAmount, uniqueKey)
                    );
                } catch (Exception e) {
                    System.err.println("Falha na transferência: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Dispara o bloqueio até que todas as 10 threads terminem de executar

        Wallet finalSource = walletRepository.findById(sourceWalletId).orElseThrow();
        Wallet finalDestination = walletRepository.findById(destinationWalletId).orElseThrow();

        System.out.println("--- RESULTADO DO TESTE DE CONCORRÊNCIA ---");
        System.out.println("Saldo Final Origem: " + finalSource.getBalance());
        System.out.println("Saldo Final Destino: " + finalDestination.getBalance());

        // Esperado: 1000 - (10 * 10) = 900.00 na origem, 1000 + (10 * 10) = 1100.00 no destino
        assertEquals(0, new BigDecimal("900.00").compareTo(finalSource.getBalance()));
        assertEquals(0, new BigDecimal("1100.00").compareTo(finalDestination.getBalance()));
    }
}
