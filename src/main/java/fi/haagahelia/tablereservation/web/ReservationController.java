package fi.haagahelia.tablereservation.web;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fi.haagahelia.tablereservation.domain.ReservationStatus;
import fi.haagahelia.tablereservation.repository.RestaurantTableRepository;
import fi.haagahelia.tablereservation.service.ReservationService;

/** Handles searching for free tables, booking, and showing a guest's reservations. */
@Controller
public class ReservationController {

    // The fixed 2-hour time slots the restaurant offers
    private static final List<String> SLOTS = List.of("16:00", "18:00", "20:00");

    private final RestaurantTableRepository tableRepository;
    private final ReservationService reservationService;

    public ReservationController(RestaurantTableRepository tableRepository,
                                 ReservationService reservationService) {
        this.tableRepository = tableRepository;
        this.reservationService = reservationService;
    }

    /** Shows the search form, and the free tables once the form has been submitted. */
    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam(required = false) Integer guests,
            Model model) {

        model.addAttribute("slots", SLOTS);

        if (date != null && time != null && guests != null) {
            // The search values are sent on so the "Book" buttons can use them
            model.addAttribute("date", date);
            model.addAttribute("time", time);
            model.addAttribute("guests", guests);
            model.addAttribute("results",
                    tableRepository.findAvailable(date, time, guests, ReservationStatus.CONFIRMED));
        }
        return "search";
    }

    /** Books the chosen table for the logged-in user (Principal = who is logged in). */
    @PostMapping("/book")
    public String book(@RequestParam Long tableId,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
                       @RequestParam int guests,
                       Principal principal,
                       RedirectAttributes redirectAttributes) {
        try {
            reservationService.book(principal.getName(), tableId, date, time, guests);
            return "redirect:/reservations?booked";
        } catch (IllegalStateException e) {
            // Show the problem on the search page
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/search";
        }
    }

    /** Shows the logged-in user's own reservations. */
    @GetMapping("/reservations")
    public String myReservations(Principal principal, Model model) {
        model.addAttribute("reservations", reservationService.findByUser(principal.getName()));
        return "reservations";
    }

        /** Cancels one of the logged-in user's reservations. */
    @PostMapping("/reservations/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancel(principal.getName(), id);
            return "redirect:/reservations?cancelled";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reservations";
        }
    }
}