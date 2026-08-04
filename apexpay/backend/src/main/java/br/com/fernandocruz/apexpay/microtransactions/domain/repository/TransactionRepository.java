package br.com.fernandocruz.apexpay.microtransactions.domain.repository;

import br.com.fernandocruz.apexpay.microtransactions.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Busca o extrato consolidado de uma conta (tanto enviadas quanto recebidas)
    // Usamos paginação (Pageable) para evitar sobrecarga de memória ao carregar milhares de transações.
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountHistory(@Param("accountId") UUID accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
            "WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) " +
            "AND t.createdAt >= :startDate " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountHistoryAndDate(
            @Param("accountId") UUID accountId,
            @Param("startDate") OffsetDateTime startDate,
            Pageable pageable
    );
}
