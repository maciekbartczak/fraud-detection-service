package dev.b6k.fds.transaction;

import dev.b6k.fds.bin.Bin;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<TransactionEntity> {
    public List<TransactionEntity> findAllByBin(Bin bin) {
        return find("bin", bin.value().toString()).list();
    }
}
