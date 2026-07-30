package br.com.fernandocruz.apexpay.microtransactions.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    // BigDecimal devido à precisão decimal
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal balance;

    /* O @Version faz com que o Hibernate controle concorrência de forma automática.
     * Se duas requisições tentarem atualizar a mesma conta ao mesmo tempo, a primeira ganha
     * e a segunda lança uma ObjectOptimisticLockingFailureException, evitando a condição de corrida.
     */
    @Version
    @Column(nullable = false)
    private Long version;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;
}
