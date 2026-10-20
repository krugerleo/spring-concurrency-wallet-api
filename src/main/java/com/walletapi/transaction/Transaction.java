package com.walletapi.transaction;


import jakarta.persistence.*;
import lombok.Data;
import com.walletapi.wallet.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "wallet_source_id")
    private Wallet walletSource;

    @ManyToOne
    @JoinColumn(name = "wallet_destination_id")
    private Wallet walletDestination;

    private LocalDateTime date;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;
}
