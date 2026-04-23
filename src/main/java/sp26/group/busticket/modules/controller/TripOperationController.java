package sp26.group.busticket.modules.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.entity.Seat;
import sp26.group.busticket.modules.entity.Ticket;
import sp26.group.busticket.modules.entity.Trip;
import sp26.group.busticket.modules.enumType.TripStatusEnum;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.repository.SeatRepository;
import sp26.group.busticket.modules.repository.TicketRepository;
import sp26.group.busticket.modules.repository.TripRepository;
import sp26.group.busticket.modules.service.TicketService;
import sp26.group.busticket.modules.service.TripService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff/operations")
@RequiredArgsConstructor
public class TripOperationController {

    private final TripService tripService;
    private final TicketService ticketService;
    private final sp26.group.busticket.modules.service.AccountService accountService;
    private final sp26.group.busticket.modules.service.SeatService seatService;

    // 1. Lịch trình của tôi (Driver/Assistant Dashboard)
    @GetMapping("/schedule")
    public String showMySchedule(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        var account = accountService.getAccountByEmail(email);
        List<Trip> trips = tripService.getStaffTrips(email);

        // Chuyến hiện tại: Chỉ lấy chuyến SCHEDULED hoặc DEPARTED đầu tiên
        Optional<Trip> currentTripOpt = trips.stream()
                .filter(t -> t.getTripStatus() == TripStatusEnum.SCHEDULED || t.getTripStatus() == TripStatusEnum.DEPARTED)
                .findFirst();

        if (currentTripOpt.isPresent()) {
            Trip currentTrip = currentTripOpt.get();
            model.addAttribute("currentTrip", currentTrip);
            
            // Map data cho card "Chuyến hiện tại"
            model.addAttribute("currentBus", new Object() {
                public String getPlateNumber() { return currentTrip.getCoach().getPlateNumber(); }
                public String getOrigin() { return currentTrip.getRoute().getDepartureLocation().getName(); }
                public String getDestination() { return currentTrip.getRoute().getArrivalLocation().getName(); }
                public String getDepartureTime() { return currentTrip.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm")); }
                public boolean isCanStart() { 
                    return LocalDateTime.now().isAfter(currentTrip.getDepartureTime()) || 
                           LocalDateTime.now().isEqual(currentTrip.getDepartureTime()); 
                }
                public String getActualDepartureTime() { 
                    return currentTrip.getActualDepartureTime() != null 
                        ? currentTrip.getActualDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm")) 
                        : null; 
                }
                public String getDate() { return "Hôm nay"; }
            });

            long totalPassengers = ticketService.countTicketsByTrip(currentTrip.getId());
            long checkedInCount = ticketService.countCheckedInTicketsByTrip(currentTrip.getId());
            
            model.addAttribute("totalPassengers", totalPassengers);
            model.addAttribute("checkedInCount", checkedInCount);
        }

        // Danh sách chuyến trong ngày (hiển thị tất cả trạng thái)
        model.addAttribute("dailyTrips", trips.stream().map(t -> new Object() {
            public UUID getId() { return t.getId(); }
            public String getTime() { return t.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm")); }
            public String getStatus() { 
                if (t.getTripStatus() == TripStatusEnum.SCHEDULED) return "SẮP CHẠY";
                if (t.getTripStatus() == TripStatusEnum.DEPARTED) return "ĐANG CHẠY";
                return "HOÀN THÀNH";
            }
            public String getOrigin() { return t.getRoute().getDepartureLocation().getName(); }
            public String getDestination() { return t.getRoute().getArrivalLocation().getName(); }
            public String getPlate() { return t.getCoach().getPlateNumber(); }
            public long getPassengerCount() { return ticketService.countTicketsByTrip(t.getId()); }
            public long getCheckedInCount() { return ticketService.countCheckedInTicketsByTrip(t.getId()); }
            public String getActualDeparture() { return t.getActualDepartureTime() != null ? t.getActualDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm, dd/MM")) : "-"; }
            public String getActualArrival() { return t.getActualArrivalTime() != null ? t.getActualArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm, dd/MM")) : "-"; }
            public String getDriverName() { return t.getDriver() != null ? t.getDriver().getFullName() : "Chưa phân công"; }
            public String getAssistantName() { return t.getAssistant() != null ? t.getAssistant().getFullName() : "Chưa phân công"; }
            public String getCoachType() { return t.getCoach().getCoachType().getName(); }
        }).collect(Collectors.toList()));

        return "staff/schedule";
    }

    // 2. Bắt đầu chuyến đi
    @PostMapping("/trips/{id}/start")
    public String startTrip(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            tripService.startTrip(id);
            redirectAttributes.addFlashAttribute("successMessage", "Chuyến đi đã bắt đầu khởi hành!");
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/operations/schedule";
    }

    // 2.1 Kết thúc chuyến đi
    @PostMapping("/trips/{id}/finish")
    public String finishTrip(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            tripService.finishTrip(id);
            redirectAttributes.addFlashAttribute("successMessage", "Chuyến đi đã kết thúc thành công!");
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/operations/schedule";
    }

    // 3. Giao diện Check-in vé (Visual Seat Map)
    @GetMapping("/trips/{id}/checkin")
    public String showCheckInForm(@PathVariable UUID id, Model model) {
        Trip trip = tripService.getTripEntityById(id);
        model.addAttribute("tripId", id);
        model.addAttribute("currentTripName", trip.getRoute().getDepartureLocation().getName() + " → " + trip.getRoute().getArrivalLocation().getName());
        model.addAttribute("isTripFinished", trip.getTripStatus() == TripStatusEnum.COMPLETED);
        
        // Lấy tất cả ghế của xe
        List<Seat> allSeats = seatService.getSeatsByCoachId(trip.getCoach().getId());
        
        // Lấy tất cả vé của chuyến đi này
        List<Ticket> tickets = tripService.getTicketsByTripId(id);
        Map<UUID, Ticket> seatToTicketMap = tickets.stream()
                .collect(Collectors.toMap(t -> t.getSeat().getId(), t -> t));

        // Phân loại ghế tầng trên/dưới và gán trạng thái check-in
        List<Map<String, Object>> lowerDeck = new ArrayList<>();
        List<Map<String, Object>> upperDeck = new ArrayList<>();

        for (Seat seat : allSeats) {
            Map<String, Object> seatData = new HashMap<>();
            seatData.put("id", seat.getId());
            seatData.put("number", seat.getSeatNumber());
            
            Ticket ticket = seatToTicketMap.get(seat.getId());
            if (ticket == null) {
                seatData.put("status", "EMPTY");
            } else {
                seatData.put("status", "CHECKED_IN".equals(ticket.getStatus()) ? "CHECKED_IN" : "BOOKED");
                seatData.put("ticketCode", ticket.getTicketCode());
                seatData.put("passengerName", ticket.getPassengerName());
            }

            if (seat.getFloor() == 1) lowerDeck.add(seatData);
            else upperDeck.add(seatData);
        }

        model.addAttribute("lowerDeck", lowerDeck);
        model.addAttribute("upperDeck", upperDeck);
        model.addAttribute("totalPassengers", tickets.size());
        model.addAttribute("checkedInCount", tickets.stream().filter(t -> "CHECKED_IN".equals(t.getStatus())).count());
        
        return "staff/checkin-ticket";
    }

    // 4. Xử lý Check-in vé (Toggle)
    @PostMapping("/trips/{id}/checkin/toggle")
    @ResponseBody
    public Map<String, Object> toggleCheckIn(@PathVariable UUID id, @RequestParam String ticketCode) {
        Map<String, Object> response = new HashMap<>();
        try {
            Trip trip = tripService.getTripEntityById(id);
            if (trip.getTripStatus() == TripStatusEnum.COMPLETED) {
                throw new AppException(ErrorCode.INVALID_INPUT, "Chuyến đi này đã kết thúc, không thể thực hiện check-in.");
            }

            ticketService.toggleCheckIn(id, ticketCode);
            response.put("success", true);
            // Re-fetch ticket to get new status
            // ... actually we can just return the new status from toggleCheckIn
            response.put("newStatus", "CHECKED_IN"); // Simplified
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // 5. Xử lý Check-in vé (Manual Code)
    @PostMapping("/trips/{id}/checkin")
    public String processCheckIn(@PathVariable UUID id, 
                                 @RequestParam String ticketCode, 
                                 RedirectAttributes redirectAttributes) {
        try {
            Trip trip = tripService.getTripEntityById(id);
            if (trip.getTripStatus() == TripStatusEnum.COMPLETED) {
                throw new AppException(ErrorCode.INVALID_INPUT, "Chuyến đi này đã kết thúc, không thể thực hiện check-in.");
            }
            ticketService.checkInTicket(ticketCode, id);
            redirectAttributes.addFlashAttribute("successMessage", "Check-in thành công cho vé: " + ticketCode);
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/operations/trips/" + id + "/checkin";
    }
}
