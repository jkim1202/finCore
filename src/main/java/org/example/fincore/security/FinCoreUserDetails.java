package org.example.fincore.security;

import lombok.Getter;
import org.example.fincore.user.entity.UserStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
public class FinCoreUserDetails implements UserDetails {
    private final Long id;
    private final String email;
    private final String passwordHash;
    private final UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;


    public FinCoreUserDetails(Long id, String email, String passwordHash, UserStatus status, Collection<? extends GrantedAuthority> authorities) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.status = Objects.requireNonNull(status);
        this.authorities = List.copyOf(Objects.requireNonNull(authorities));
    }


    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return passwordHash;
    }

    @Override
    public @NonNull String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled(){
        return status.equals(UserStatus.ACTIVE);
    }
}
