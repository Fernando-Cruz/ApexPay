package br.com.fernandocruz.apexpay.microtransactions.domain.repository;

import br.com.fernandocruz.apexpay.microtransactions.domain.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {

    // Permite verificar em pouquíssimos milissegundos se a chave enviada no cabeçalho HTTP já existe
    Optional<IdempotencyKey> findByClientKey(String clientKey);
}
