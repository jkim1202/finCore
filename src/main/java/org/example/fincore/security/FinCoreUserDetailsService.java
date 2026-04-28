package org.example.fincore.security;

import org.example.fincore.user.entity.User;
import org.example.fincore.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinCoreUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public FinCoreUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();

        return new FinCoreUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus(),
                authorities
        );
    }
}
