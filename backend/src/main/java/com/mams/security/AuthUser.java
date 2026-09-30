package com.mams.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class AuthUser implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final String roleName;
    private final Long baseId;
    private final String status;
    private Long sessionId;

    public AuthUser(Long id, String username, String password, String roleName, Long baseId, String status, Long sessionId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roleName = roleName;
        this.baseId = baseId;
        this.status = status;
        this.sessionId = sessionId;
    }

    public Long getId() { return id; }
    public String getRoleName() { return roleName; }
    public Long getBaseId() { return baseId; }
    public String getStatus() { return status; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return "ACTIVE".equals(status); }
}
