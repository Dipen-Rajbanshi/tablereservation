package fi.haagahelia.tablereservation.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests who is allowed to open which page. MockMvc sends fake web requests
 * through the real security rules, without starting a server.
 */
@SpringBootTest
class SecurityAccessTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Build MockMvc with Spring Security switched on
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void anonymousVisitorIsSentToLoginFromAdminPage() throws Exception {
        mockMvc.perform(get("/admin/tables"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void anonymousVisitorIsSentToLoginFromSearchPage() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void guestCannotOpenAdminPage() throws Exception {
        mockMvc.perform(get("/admin/tables").with(user("guest@test.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanOpenAdminPage() throws Exception {
        mockMvc.perform(get("/admin/tables").with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void tablesPageIsPublic() throws Exception {
        mockMvc.perform(get("/tables"))
                .andExpect(status().isOk());
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void formWithoutSecurityTokenIsRejected() throws Exception {
        // A POST without the hidden CSRF token must be refused
        mockMvc.perform(post("/register")
                        .param("name", "Test")
                        .param("email", "new@test.com")
                        .param("password", "secret123"))
                .andExpect(status().isForbidden());
    }
}