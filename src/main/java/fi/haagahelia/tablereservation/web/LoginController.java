package fi.haagahelia.tablereservation.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Shows the login page. The login itself is handled by Spring Security. */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";   // means templates/login.html
    }
}