package com.walletapi;

import com.walletapi.wallet.Wallet;
import com.walletapi.wallet.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class WalletConcurrencyApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalletConcurrencyApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(WalletRepository walletRepository) {
        return args -> {
            Wallet walletA = new Wallet();
            walletA.setBalance(new BigDecimal("1000.00"));
            walletRepository.save(walletA);

            Wallet walletB = new Wallet();
            walletB.setBalance(new BigDecimal("1000.00"));
            walletRepository.save(walletB);
        };
    }
}
