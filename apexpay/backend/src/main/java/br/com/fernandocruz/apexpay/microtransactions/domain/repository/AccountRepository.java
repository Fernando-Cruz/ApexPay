package br.com.fernandocruz.apexpay.microtransactions.domain.repository;

import br.com.fernandocruz.apexpay.microtransactions.domain.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithPessimisticLock(@Param("id") UUID id);

    Optional<Account> findByUserId(UUID userId);

    @org.springframework.data.jpa.repository.Query("""
        SELECT a FROM Account a 
        WHERE a.accountNumber = :identifier 
           OR CAST(a.id AS string) = :identifier 
           OR CAST(a.userId AS string) = :identifier
    """)
    Optional<Account> findByIdentifier(String identifier);
}
