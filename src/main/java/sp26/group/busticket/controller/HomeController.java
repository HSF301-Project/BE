package sp26.group.busticket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import sp26.group.busticket.dto.trip.request.TripSearchRequestDTO;
import sp26.group.busticket.dto.trip.response.TripSearchResultDTO;
import sp26.group.busticket.entity.Location;
import sp26.group.busticket.service.BookingService;
import sp26.group.busticket.service.LocationService;
import sp26.group.busticket.service.TripService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TripService tripService;
    private final LocationService locationService;
    private final BookingService bookingService;

    @GetMapping({"/", "/home"})
    public String homepage(Model model) {
        TripSearchRequestDTO searchForm = TripSearchRequestDTO.builder()
                .date(LocalDate.now().toString())
                .build();
        model.addAttribute("searchForm", searchForm);
        List<String> cities = locationService.getLocationsByType("TERMINAL").stream()
                .map(Location::getCity)
                .distinct()
                .collect(Collectors.toList());
        model.addAttribute("cities", cities);
        model.addAttribute("popularRoutes", bookingService.getTopPopularRoutesAllTime(5));
        return "Passenger/basic/homepage";
    }

    @GetMapping("/search")
    public String searchTrips(@ModelAttribute("searchForm") TripSearchRequestDTO searchForm, Model model) {
        List<String> cities = locationService.getLocationsByType("TERMINAL").stream()
                .map(Location::getCity)
                .distinct()
                .collect(Collectors.toList());
        model.addAttribute("cities", cities);
        
        TripSearchResultDTO searchResult = tripService.searchTrips(searchForm);
        model.addAttribute("searchResult", searchResult);
        model.addAttribute("filterForm", searchForm);
        return "Passenger/basic/search&result_page";
    }
}
