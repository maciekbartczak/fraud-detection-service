package dev.b6k.fds.transaction;


import dev.b6k.fds.transaction.riskassessment.RiskLevel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TRANSACTIONS")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "BIN")
    private String bin;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "COUNTRY_CODE")
    private String countryCode;

    @Column(name = "RISK_SCORE")
    private int riskScore;

    @Column(name = "RISK_LEVEL")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(name = "TIMESTAMP")
    private LocalDateTime timestamp;
}