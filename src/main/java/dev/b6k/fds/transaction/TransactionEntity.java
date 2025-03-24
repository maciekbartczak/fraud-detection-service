package dev.b6k.fds.transaction;


import dev.b6k.fds.transaction.riskassessment.RiskLevel;
import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactorEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TRANSACTIONS")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TransactionEntity {
    @Id
    @Column(name = "ID")
    @Builder.Default
    @EqualsAndHashCode.Include
    private UUID id = UUID.randomUUID();

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

    @ToString.Exclude
    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<RiskFactorEntity> riskFactors;
}