package com.walletapi.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.walletapi.wallet.Wallet;
import com.walletapi.wallet.WalletRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public Transaction transfer(TransactionRequestDTO request) {
        Optional<Transaction> transactionCheck = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
        if (transactionCheck.isPresent()) {
            log.info("Transaction with id {} already processed", request.idempotencyKey());
            return transactionCheck.get();
        }

        Wallet sourceWallet = walletRepository.findByIdWithLock(request.sourceWallet()).orElseThrow(
                () -> new IllegalArgumentException("Source wallet not found.")
        );
        Wallet destinationWallet = walletRepository.findByIdWithLock(request.destinationWallet()).orElseThrow(
                () -> new IllegalArgumentException("Destination wallet nor found.")
        );

        if (sourceWallet.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds.");
        }

        sourceWallet.setBalance(sourceWallet.getBalance().subtract(request.amount()));
        destinationWallet.setBalance(destinationWallet.getBalance().add(request.amount()));

        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(request.idempotencyKey());
        transaction.setWalletSource(sourceWallet);
        transaction.setWalletDestination(destinationWallet);
        transaction.setAmount(request.amount());
        transaction.setDate(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }
}
