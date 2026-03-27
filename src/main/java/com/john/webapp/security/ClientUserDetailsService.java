package com.john.webapp.security;

import com.john.webapp.dto.ClientResponseDto;
import com.john.webapp.service.ClientServiceClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientUserDetailsService implements UserDetailsService {

    private final ClientServiceClient clientService;

    public ClientUserDetailsService(ClientServiceClient clientService) {
        this.clientService = clientService;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        List<ClientResponseDto> all = clientService.getAllClients();

        ClientResponseDto client = all.stream()
                .filter(c -> username.equalsIgnoreCase(c.getEmail()))
                .findFirst()
                .orElseThrow(() -> {
                    return new UsernameNotFoundException(
                            "Користувача з email '" + username + "' не знайдено.");
                });

        String role = (client.getRole() != null && client.getRole() == 1)
                ? "ROLE_ADMIN"
                : "ROLE_CLIENT";

        return User.builder()
                .username(client.getEmail())
                .password(client.getPass())
                .authorities(new SimpleGrantedAuthority(role))
                .build();
    }
}