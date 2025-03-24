package dev.b6k.fds.transaction.riskassessment.riskfactor;


import dev.b6k.fds.transaction.TransactionEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TRANSACTION_RISK_FACTORS")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RiskFactorEntity {
    @Id
    @Column(name = "ID")
    @Builder.Default
    @EqualsAndHashCode.Include
    private UUID id = UUID.randomUUID();

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SCORE")
    private Integer score;

    @Column(name = "WEIGHT")
    private Double weight;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "TRANSACTION_ID")
    private TransactionEntity transaction;
}