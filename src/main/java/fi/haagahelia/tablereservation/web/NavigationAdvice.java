package fi.haagahelia.tablereservation.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Runs for every controller and adds "isAdmin" to the page data,
 * so templates can show staff links only to staff.
 */
@ControllerAdvice
public class NavigationAdvice {

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
