package fi.haagahelia.tablereservation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import fi.haagahelia.tablereservation.repository.RestaurantTableRepository;

/** Handles the pages that show the restaurant's tables. */
@Controller
public class TableController {

    private final RestaurantTableRepository tableRepository;

    // Spring passes the repository in automatically (constructor injection)
    public TableController(RestaurantTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    // The home page just sends the visitor to the table list
    @GetMapping("/")
    public String home() {
        return "redirect:/tables";
    }

    // Loads all tables from the database and shows them on tables.html
    @GetMapping("/tables")
    public String listTables(Model model) {
        model.addAttribute("tables", tableRepository.findAll());
        return "tables";   // means templates/tables.html
    }
}