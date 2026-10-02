package fi.haagahelia.tablereservation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import fi.haagahelia.tablereservation.domain.AppUser;
import fi.haagahelia.tablereservation.domain.RestaurantTable;
import fi.haagahelia.tablereservation.domain.Role;
import fi.haagahelia.tablereservation.repository.AppUserRepository;
import fi.haagahelia.tablereservation.repository.RestaurantTableRepository;
import java.util.TimeZone;

@SpringBootApplication
public class TablereservationApplication {

        public static void main(String[] args) {
        // The restaurant works in Finnish time. This also avoids a PostgreSQL error
        // caused by the old time zone name "Europe/Kiev" that Windows can report.
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Helsinki"));
        SpringApplication.run(TablereservationApplication.class, args);
    }

    /**
     * Runs once at startup and adds sample data, but only what is missing,
     * so restarting with a persistent database does not create duplicates.
     */
    @Bean
    public CommandLineRunner demoData(RestaurantTableRepository tableRepository,
                                      AppUserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (tableRepository.count() == 0) {
                tableRepository.save(new RestaurantTable(1, 2, true));
                tableRepository.save(new RestaurantTable(2, 2, true));
                tableRepository.save(new RestaurantTable(3, 4, true));
                tableRepository.save(new RestaurantTable(4, 4, true));
                tableRepository.save(new RestaurantTable(5, 6, true));
            }

            // Demo admin for development only. The password is hashed like any other.
            if (userRepository.findByEmail("admin@restaurant.com").isEmpty()) {
                userRepository.save(new AppUser("Admin", "admin@restaurant.com",
                        passwordEncoder.encode("admin123"), Role.ADMIN));
            }
        };
    }
}