package com.mediatheque.auth_svc.config;

import com.mediatheque.auth_svc.model.Role;
import com.mediatheque.auth_svc.model.User;
import com.mediatheque.auth_svc.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppConfigTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppConfig appConfig;


    @Test
    void userDetailsService_shouldReturnUser_whenEmailExists() {
        User user = User.builder()
                .id(1L)
                .email("alice@example.com")
                .password("encoded_password")
                .role(Role.USER)
                .enabled(true)
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        UserDetailsService service = appConfig.userDetailsService();
        var result = service.loadUserByUsername("alice@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("alice@example.com");
        verify(userRepository).findByEmail("alice@example.com");
    }

    @Test
    void userDetailsService_shouldThrow_whenEmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        UserDetailsService service = appConfig.userDetailsService();

        assertThatThrownBy(() -> service.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: unknown@example.com");
    }


    @Test
    void passwordEncoder_shouldReturnBCryptInstance() {
        PasswordEncoder encoder = appConfig.passwordEncoder();

        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void passwordEncoder_shouldEncodePassword() {
        PasswordEncoder encoder = appConfig.passwordEncoder();
        String encoded = encoder.encode("secret123");

        assertThat(encoded).isNotEqualTo("secret123");
        assertThat(encoder.matches("secret123", encoded)).isTrue();
    }

    @Test
    void passwordEncoder_shouldProduceDifferentHashes_forSamePassword() {
        PasswordEncoder encoder = appConfig.passwordEncoder();
        String encoded1 = encoder.encode("secret123");
        String encoded2 = encoder.encode("secret123");

        assertThat(encoded1).isNotEqualTo(encoded2);
    }


    @Test
    void authenticationProvider_shouldReturnDaoAuthenticationProvider() {
        AuthenticationProvider provider = appConfig.authenticationProvider();

        assertThat(provider).isNotNull();
    }
}