package fi.haagahelia.tablereservation.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** A person who can log in: either a guest (USER) or staff (ADMIN). */
@Entity
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    @Column(unique = true)      // two users cannot have the same email
    private String email;

    @NotBlank
    private String passwordHash;    // will hold a BCrypt hash, never a plain password

    @Enumerated(EnumType.STRING)    // saves "USER" or "ADMIN" as text
    private Role role;

    public AppUser() {
    }

    public AppUser(@NotBlank String name, @Email @NotBlank String email, @NotBlank String passwordHash, Role role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}