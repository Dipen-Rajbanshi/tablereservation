package fi.haagahelia.tablereservation.security;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import fi.haagahelia.tablereservation.domain.AppUser;
import fi.haagahelia.tablereservation.domain.Role;
import fi.haagahelia.tablereservation.repository.AppUserRepository;

/**
 * Called after Google has confirmed who the visitor is. It finds the matching
 * AppUser by email, or creates a new one with the USER role, and gives Spring
 * Security the roles from our own database.
 */
@Service
public class CustomOidcUserService extends OidcUserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOidcUserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Let Spring fetch the Google profile first
        OidcUser oidcUser = super.loadUser(userRequest);

        // Only accept Google accounts whose email has been verified
        if (oidcUser.getEmail() == null || !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_verified"), "Your Google email is not verified.");
        }
        String email = oidcUser.getEmail().trim().toLowerCase();

        // Existing user (same email) is reused, otherwise a new guest account is created
        AppUser user = userRepository.findByEmail(email).orElseGet(() -> {
            String name = (oidcUser.getFullName() != null) ? oidcUser.getFullName() : email;
            // Google users never log in with a password, so it is a random hashed value
            String unusablePassword = passwordEncoder.encode(UUID.randomUUID().toString());
            return userRepository.save(new AppUser(name, email, unusablePassword, Role.USER));
        });

        // Roles come from our database. "email" becomes the name used by Principal.
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "email");
    }
}