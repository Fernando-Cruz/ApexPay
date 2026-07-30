package br.com.fernandocruz.apexpay.microtransactions.domain.repository;

import br.com.fernandocruz.apexpay.microtransactions.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Método crucial para a autenticação e login de usuários
    Optional<User> findByEmail(String email);

    // Método auxiliar para evitar cadastros duplicados
    boolean existsByEmail(String email);
}
