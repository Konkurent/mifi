package mifi.auth.service;

import lombok.RequiredArgsConstructor;
import mifi.auth.dao.entities.AccessEntity;
import mifi.auth.dao.repositories.AccessRepository;
import mifi.auth.dto.CreateUserEvent;
import mifi.auth.dto.UpdateUserPayload;
import mifi.auth.exception.AccessServiceException;
import mifi.auth.controllers.advise.SystemCode;
import mifi.auth.security.dto.CustomerDetails;
import mifi.auth.dto.SignUpPayload;
import mifi.auth.dto.User;
import mifi.auth.security.jwt.JwtService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccessService implements UserDetailsService {

    private final AccessRepository accessRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtService jwtService;

    public void createAccess(CreateUserEvent event) {
        AccessEntity access = AccessEntity.builder()
                .email(event.email())
                .userId(event.userId())
                .login(event.login())
                .role("USER")
                .password(passwordEncoder.encode(event.password()))
                .build();
        accessRepository.save(access);
    }

    public String signUp(SignUpPayload payload) {
        User user = userService.createUser(payload);
        AccessEntity access = AccessEntity.builder()
                .email(payload.email())
                .userId(user.id())
                .login(payload.login())
                .role("USER")
                .password(passwordEncoder.encode(payload.password()))
                .build();
        accessRepository.save(access);
        return jwtService.generateToken((CustomerDetails) loadUserByUsername(payload.email()));
    }

    public String updateAccess(UpdateUserPayload payload) {
        AccessEntity access = accessRepository.getByUserId(payload.userId());
        if (access == null) {
            throw new AccessServiceException("Access not found!", SystemCode.NOT_FOUND);
        }
        access.setEmail(payload.email().trim());
        access.setLogin(payload.login() != null && !payload.login().isBlank() ? payload.login() : null);
        accessRepository.save(access);
        return jwtService.generateToken((CustomerDetails) loadUserByUsername(access.getEmail()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accessRepository.findByUserName(username)
                .or(() -> loadExternalUserByLogin(username))
                .map(access -> CustomerDetails.builder()
                        .password(access.getPassword())
                        .userId(access.getUserId())
                        .email(access.getEmail())
                        .authorities(List.of(new SimpleGrantedAuthority(access.getRole())))
                        .build()
                ).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    private Optional<AccessEntity> loadExternalUserByLogin(String username) {
        User user = userService.getUserByLogin(username);
        if (user == null) return Optional.empty();
        AccessEntity entity = accessRepository.findByUserName(user.email().trim()).orElse(null);
        if (entity == null) return Optional.empty();
        entity.setLogin(username);
        accessRepository.save(entity);
        return Optional.of(entity);
    }

    public void deleteAccess(String email) {
        if (accessRepository.existsById(email)) {
            accessRepository.deleteById(email);
        }
    }
}
