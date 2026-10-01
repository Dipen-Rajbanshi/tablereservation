package fi.haagahelia.tablereservation.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fi.haagahelia.tablereservation.domain.RestaurantTable;
import fi.haagahelia.tablereservation.repository.RestaurantTableRepository;
import fi.haagahelia.tablereservation.service.ReservationService;

/** Pages for staff. SecurityConfig only lets users with the ADMIN role in. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final RestaurantTableRepository tableRepository;
    private final ReservationService reservationService;

    public AdminController(RestaurantTableRepository tableRepository,
                           ReservationService reservationService) {
        this.tableRepository = tableRepository;
        this.reservationService = reservationService;
    }

    /** Lists all tables, with a form for adding a new one. */
    @GetMapping("/tables")
    public String tables(Model model) {
        model.addAttribute("tables", tableRepository.findAll());
        return "admin-tables";
    }

    /** Adds a new table. */
    @PostMapping("/tables")
    public String addTable(@RequestParam int tableNumber,
                           @RequestParam int seats,
                           RedirectAttributes redirectAttributes) {
        if (tableNumber < 1 || seats < 1) {
            redirectAttributes.addFlashAttribute("error", "Table number and seats must be at least 1.");
        } else if (tableRepository.existsByTableNumber(tableNumber)) {
            redirectAttributes.addFlashAttribute("error", "A table with that number already exists.");
        } else {
            tableRepository.save(new RestaurantTable(tableNumber, seats, true));
        }
        return "redirect:/admin/tables";
    }

    /** Switches a table on or off. Disabled tables do not appear in searches. */
    @PostMapping("/tables/{id}/toggle")
    public String toggleTable(@PathVariable Long id) {
        tableRepository.findById(id).ifPresent(table -> {
            table.setActive(!table.isActive());
            tableRepository.save(table);
        });
        return "redirect:/admin/tables";
    }

    /** Shows all reservations of one day. If no date is given, today is used. */
    @GetMapping("/reservations")
    public String reservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        LocalDate day = (date != null) ? date : LocalDate.now();
        model.addAttribute("date", day);
        model.addAttribute("reservations", reservationService.findByDate(day));
        return "admin-reservations";
    }

    /** Cancels a reservation, then returns to the same day's list. */
    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable Long id,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                    RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelAsStaff(id);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reservations?date=" + date;
    }
}