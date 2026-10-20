package wallet;

import jakarta.persistence.*;
import lombok.Data;
import user.User;

import java.math.BigDecimal;

@Entity
@Data
public class Wallet{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal balance;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
