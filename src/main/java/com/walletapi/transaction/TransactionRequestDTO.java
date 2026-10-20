package com.walletapi.transaction;


import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;


public record TransactionRequestDTO(
    @NotNull(message = "Source wallet id is required.")
    Long sourceWallet,

    @NotNull(message = "Destination wallet id is required.")
    Long destinationWallet,

    @NotNull(message = "Transaction amount is required.")
    @Positive(message = "Transaction amount needs to be positive.")
    BigDecimal amount
){}
