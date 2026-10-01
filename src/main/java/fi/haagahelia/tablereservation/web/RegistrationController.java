package fi.haagahelia.tablereservation.web;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fi.haagahelia.tablereservation.domain.AppUser;
import fi.haagahelia.tablereservation.domain.Role;
import fi.haagahelia.tablereservation.repository.AppUserRepository;

/** Lets a new guest create an account. */
@Controller
public class RegistrationController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Shows the empty registration form
    @GetMapping("/register")
    public String showForm() {
        return "register";
    }

    // Handles the submitted form
    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {

        String cleanEmail = email.trim().toLowerCase();

        // Basic checks; on a problem, show the form again with a message
        if (name.isBlank() || cleanEmail.isBlank() || password.length() < 6) {
            model.addAttribute("error", "Please fill in all fields. The password needs at least 6 characters.");
            return "register";
        }
        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            model.addAttribute("error", "An account with this email already exists.");
            return "register";
        }

        // Hash the password, then save the new user with the USER role
        AppUser user = new AppUser(name.trim(), cleanEmail, passwordEncoder.encode(password), Role.USER);
        userRepository.save(user);

        return "redirect:/register?success";
    }
}
