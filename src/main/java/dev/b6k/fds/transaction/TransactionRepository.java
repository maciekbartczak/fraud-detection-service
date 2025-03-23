package dev.b6k.fds.transaction;

import dev.b6k.fds.bin.Bin;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<TransactionEntity> {
    public TransactionStatistics getTransactionsStatistics(Bin bin) {
        var result = getEntityManager()
                .createQuery("select count(t), avg(t.amount) from TransactionEntity t where t.bin = :bin", Object[].class)
                .setParameter("bin", bin.value().toString())
                .getSingleResult();
        var totalCount = ((Number) result[0]);
        var averageAmount = ((Double) result[1]);

        if (totalCount.intValue() == 0) {
            return new TransactionStatistics.NoTransactions();
        }

        return new TransactionStatistics.WithTransactions(
                totalCount.intValue(),
                Optional.ofNullable(averageAmount)
                        .map(it -> BigDecimal.valueOf(it).setScale(2, RoundingMode.HALF_UP))
                        .orElse(BigDecimal.ZERO)
        );
    }

    public sealed interface TransactionStatistics {
        public record NoTransactions() implements TransactionStatistics {
        }

        public record WithTransactions(int totalCount, BigDecimal averageAmount) implements TransactionStatistics {
        }
    }
}
