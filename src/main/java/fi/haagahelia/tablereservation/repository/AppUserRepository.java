package fi.haagahelia.tablereservation.repository;

import fi.haagahelia.tablereservation.domain.AppUser;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface AppUserRepository extends CrudRepository<AppUser, Long> {

    // Spring builds the SQL from the method name: SELECT ... WHERE email = ?
    // We will need this for login.
    Optional<AppUser> findByEmail(String email);
}
