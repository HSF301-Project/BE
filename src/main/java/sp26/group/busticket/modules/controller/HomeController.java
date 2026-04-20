package sp26.group.busticket.modules.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import sp26.group.busticket.modules.dto.trip.request.TripSearchRequestDTO;
import sp26.group.busticket.modules.dto.trip.response.TripSearchResultDTO;
import sp26.group.busticket.modules.service.LocationService;
import sp26.group.busticket.modules.service.TripService;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TripService tripService;
    private final LocationService locationService;

    @GetMapping({"/", "/home"})
    public String homepage(Model model) {
        TripSearchRequestDTO searchForm = TripSearchRequestDTO.builder()
                .date(LocalDate.now().toString())
                .build();
        model.addAttribute("searchForm", searchForm);
        model.addAttribute("locations", locationService.getAllLocations());
        // Featured routes can be added here
        return "Passenger/basic/homepage";
    }

    @GetMapping("/search")
    public String searchTrips(@ModelAttribute("searchForm") TripSearchRequestDTO searchForm, Model model) {
        if (searchForm.getDate() == null || searchForm.getDate().isEmpty()) {
            searchForm.setDate(LocalDate.now().toString());
        }
        
        TripSearchResultDTO searchResult = tripService.searchTrips(searchForm);
        model.addAttribute("searchResult", searchResult);
        model.addAttribute("filterForm", searchForm); // Using same DTO for filters
        return "Passenger/basic/search&result_page";
    }
}
