package sp26.group.busticket.modules.controller.staff;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sp26.group.busticket.modules.entity.TicketEntity;
import sp26.group.busticket.modules.entity.TripEntity;
import sp26.group.busticket.modules.repository.CoachRepository;
import sp26.group.busticket.modules.repository.RouteRepository;
import sp26.group.busticket.modules.repository.TicketRepository;
import sp26.group.busticket.modules.repository.TripRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {
    
    private final CoachRepository coachRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "staff/dashboard";
    }
    
    @GetMapping("/coaches")
    public String listCoaches(Model model) {
        model.addAttribute("coaches", coachRepository.findAll());
        return "staff/coaches";
    }
    
    @GetMapping("/routes")
    public String listRoutes(Model model) {
        model.addAttribute("routes", routeRepository.findAllWithLocations());
        return "staff/routes";
    }
    
    @GetMapping("/trips")
    public String listTrips(
            @RequestParam(required = false) Integer routeId,
            @RequestParam(required = false) LocalDate date,
            Model model) {
        
        List<TripEntity> trips;
        model.addAttribute("routes", routeRepository.findAllWithLocations());
        model.addAttribute("selectedRouteId", routeId);
        model.addAttribute("selectedDate", date);
        
        if (routeId != null && date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            trips = tripRepository.findByRouteIdAndDepartureTimeBetween(routeId, start, end);
        } else if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            trips = tripRepository.findByDepartureTimeBetween(start, end);
        } else {
            trips = tripRepository.findAll();
        }
        model.addAttribute("trips", trips);
        return "staff/trips";
    }
    
    @GetMapping("/tickets/search")
    public String searchTicketsForm() {
        return "staff/ticket-search";
    }
    
    @GetMapping("/tickets/search/by-code")
    public String searchByCode(
            @RequestParam String ticketCode,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        return ticketRepository.findByTicketCodeWithDetails(ticketCode)
                .map(ticket -> {
                    model.addAttribute("ticket", ticket);
                    return "staff/ticket-detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Khong tim thay ve: " + ticketCode);
                    return "redirect:/staff/tickets/search";
                });
    }
    
    @GetMapping("/tickets/search/by-phone")
    public String searchByPhone(
            @RequestParam String phone,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        List<TicketEntity> tickets = ticketRepository.findByAccountPhoneWithDetails(phone);
        if (tickets.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Khong tim thay ve: " + phone);
            return "redirect:/staff/tickets/search";
        }
        model.addAttribute("tickets", tickets);
        model.addAttribute("phone", phone);
        return "staff/ticket-list";
    }
}
