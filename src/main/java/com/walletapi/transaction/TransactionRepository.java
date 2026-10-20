package com.walletapi.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import com.walletapi.wallet.Wallet;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletSourceOrWalletDestination(Wallet walletSource, Wallet walletDestination);
}
