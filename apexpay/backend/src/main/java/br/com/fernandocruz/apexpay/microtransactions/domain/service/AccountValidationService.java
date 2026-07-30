package br.com.fernandocruz.apexpay.microtransactions.domain.service;

import br.com.fernandocruz.apexpay.microtransactions.application.dto.AccountValidationResponseDTO;
import br.com.fernandocruz.apexpay.microtransactions.domain.model.Account;
import br.com.fernandocruz.apexpay.microtransactions.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountValidationService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public AccountValidationResponseDTO validateByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> new AccountValidationResponseDTO(
                        true,
                        account.getId(),
                        account.getAccountNumber(),
                        account.getUserId()
                ))
                .orElseGet(() -> new AccountValidationResponseDTO(false, null, accountNumber, null));
    }
}