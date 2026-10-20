package com.walletapi.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import com.walletapi.wallet.Wallet;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletSourceOrWalletDestination(Wallet walletSource, Wallet walletDestination);
    Optional<Transaction> findByIdempotencyKey(String empotencyKey);
}
