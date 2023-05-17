package eformer.back.eformer_backend.utility.auth;

import eformer.back.eformer_backend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class AppConfig {
    private final UserRepository usersManager;

    public AppConfig(UserRepository usersManager) {
        this.usersManager = usersManager;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usersManager.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(username + " not found")
        );
    }
}
