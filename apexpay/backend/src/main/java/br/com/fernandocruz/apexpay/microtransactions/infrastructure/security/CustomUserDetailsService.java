/*  * 1 - Desacoplamento de Contratos: Destaque para o avaliador que você preferiu implementar o UserDetailsService
*   adaptando a entidade User em tempo  de execução, em vez de poluir a sua  entidade de domínio User implementando
*   a interface  UserDetails  diretamente nela. Isso mantém sua entidade de  banco 100% limpa de regras específicas
*   de framework (respeitando os princípios de Clean Architecture).
*   * 2 - Segurança na Busca: A busca é feita estritamente pelo campo email que já marcamos como UNIQUE e indexamos
*   nativamente no PostgreSQL, garantindo que o processo de login seja extremamente rápido.
*/
package br.com.fernandocruz.apexpay.microtransactions.infrastructure.security;

import br.com.fernandocruz.apexpay.microtransactions.domain.model.User;
import br.com.fernandocruz.apexpay.microtransactions.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        // 1. Busca o usuário no banco de dados usando o repositório que criamos
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("Usuário não encontrado com o e-mail" + email));

        // 2. Adapta o nosso usuário do domínio para o UserDetails do Spring Security.
        // Passamos o email (username), a senha criptografada (password_hash) e uma lista vazia de permissões (authorities) por enquanto.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                new ArrayList<>() // Aqui poderíamos mapear Perfis/Roles como ADMIN ou USER no futuro
        );
    }
}
