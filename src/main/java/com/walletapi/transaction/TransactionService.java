package com.walletapi.transaction;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.walletapi.wallet.Wallet;
import com.walletapi.wallet.WalletRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public Transaction transfer(TransactionRequestDTO request) {
        Wallet sourceWallet = walletRepository.findById(request.sourceWallet()).orElseThrow(
                () -> new IllegalArgumentException("Source wallet not found.")
        );
        Wallet destinationWallet = walletRepository.findById(request.destinationWallet()).orElseThrow(
                () -> new IllegalArgumentException("Destination wallet nor found.")
        );

        if (sourceWallet.getBalance().compareTo(destinationWallet.getBalance()) < 0) {
            throw new IllegalArgumentException("Insufficient funds.");
        }

        sourceWallet.setBalance(sourceWallet.getBalance().subtract(request.amount()));
        destinationWallet.setBalance(destinationWallet.getBalance().add(request.amount()));

        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);
        Transaction transaction = new Transaction();
        transaction.setWalletSource(sourceWallet);
        transaction.setWalletDestination(destinationWallet);
        transaction.setAmount(transaction.getAmount());
        transaction.setDate(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }
}
