package com.bubblesessence.seguridad.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Principal para clientes (tipo=CLIENTE). Almacena el ID del cliente
 * para que el endpoint pueda extraerlo y filtrar sus pedidos.
 */
public class ClientePrincipal implements UserDetails {
    private final Long id;
    private final String documento;
    private final Collection<GrantedAuthority> authorities;

    public ClientePrincipal(Long id, String documento) {
        this.id = id;
        this.documento = documento;
        this.authorities = new ArrayList<>();
        this.authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return documento;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
