package fi.haagahelia.tablereservation.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Security settings: which pages are public, which need login, and how login works. */
@Configuration
public class SecurityConfig {

    // Hashes passwords with BCrypt; used when registering users
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public pages: anyone can open these
                .requestMatchers("/", "/tables", "/register", "/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Everything else needs a logged-in user
                
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")             // our own login page
                .usernameParameter("email")      // the login form field is called "email"
                .defaultSuccessUrl("/search", true)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }
}