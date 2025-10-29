package mifi.auth.security.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Data
@Builder
public class CustomerDetails implements UserDetails {

    private final String email;
    private final Long userId;
    private final String password;
    private final List<GrantedAuthority> authorities;

    @Override
    public String getUsername() {
        return email;
    }

}
