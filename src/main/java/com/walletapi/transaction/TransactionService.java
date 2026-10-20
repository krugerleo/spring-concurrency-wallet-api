package com.walletapi.transaction;

import org.springframework.transaction.annotation.Transactional;
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
        transaction.setWalletSource(sourceWallet);
        transaction.setWalletDestination(destinationWallet);
        transaction.setAmount(request.amount());
        transaction.setDate(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }
}
