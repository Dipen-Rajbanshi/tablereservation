package fi.haagahelia.tablereservation.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/** Security settings: which pages are public, which need login, and how login works. */
@Configuration
public class SecurityConfig {

    // Hashes passwords with BCrypt; used when registering users
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomOidcUserService oidcUserService) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public pages: anyone can open these
                .requestMatchers("/", "/tables", "/register", "/login", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                // Only staff may open anything under /admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Everything else needs a logged-in user
                .anyRequest().authenticated())
            // Login with email and password
            .formLogin(form -> form
                .loginPage("/login")             // our own login page
                .usernameParameter("email")      // the login form field is called "email"
                .successHandler(roleBasedSuccessHandler())
                .permitAll())
            // Login with Google
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService))
                .successHandler(roleBasedSuccessHandler()))
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }

    /** After any kind of login: staff go to the reservations list, guests to the search page. */
    private AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(isAdmin ? "/admin/reservations" : "/search");
        };
    }
}