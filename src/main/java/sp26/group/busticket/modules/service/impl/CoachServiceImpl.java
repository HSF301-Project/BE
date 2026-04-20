package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.coach.request.CoachRequestDTO;
import sp26.group.busticket.modules.dto.coach.response.CoachResponseDTO;
import sp26.group.busticket.modules.dto.coach.response.CoachDetailResponseDTO;
import sp26.group.busticket.modules.dto.coach.response.PassengerDetailDTO;
import sp26.group.busticket.modules.dto.coach.response.TripDetailDTO;
import sp26.group.busticket.modules.entity.Coach;
import sp26.group.busticket.modules.entity.Ticket;
import sp26.group.busticket.modules.mapper.CoachMapper;
import sp26.group.busticket.modules.repository.CoachRepository;
import sp26.group.busticket.modules.repository.TicketRepository;
import sp26.group.busticket.modules.repository.TripRepository;
import sp26.group.busticket.modules.service.CoachService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachRepository coachRepository;
    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    private final CoachMapper coachMapper;

    @Override
    public CoachResponseDTO createCoach(CoachRequestDTO request) {
        if (coachRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new AppException(ErrorCode.PLATE_NUMBER_ALREADY_EXISTS);
        }
        Coach coach = coachMapper.toEntity(request);
        return coachMapper.toResponse(coachRepository.save(coach));
    }

    @Override
    public List<CoachResponseDTO> getAllCoaches() {
        return coachRepository.findAll().stream()
                .map(coachMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CoachResponseDTO getCoachById(UUID id) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND));
        return coachMapper.toResponse(coach);
    }

    @Override
    public CoachResponseDTO updateCoach(UUID id, CoachRequestDTO request) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND));

        coachRepository.findByPlateNumber(request.getPlateNumber())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new AppException(ErrorCode.PLATE_NUMBER_ALREADY_EXISTS);
                    }
                });

        coachMapper.updateEntity(coach, request);
        return coachMapper.toResponse(coachRepository.save(coach));
    }

    @Override
    public void deleteCoach(UUID id) {
        if (!coachRepository.existsById(id)) {
            throw new AppException(ErrorCode.COACH_NOT_FOUND);
        }
        
        // Kiểm tra xem xe có khách hàng đã đặt vé không
        if (ticketRepository.existsByCoachIdDirect(id)) {
            throw new AppException(ErrorCode.INVALID_INPUT, 
                "Không thể xóa xe này vì đã có hành khách đặt vé trên các chỗ ngồi của xe. Vui lòng kiểm tra lại!");
        }

        // Kiểm tra xem xe có đang nằm trong chuyến đi nào không
        if (tripRepository.existsByCoach_Id(id)) {
            throw new AppException(ErrorCode.INVALID_INPUT, 
                "Không thể xóa xe này vì đang có chuyến đi liên quan!");
        }
        
        coachRepository.deleteById(id);
    }

    @Override
    public CoachDetailResponseDTO getCoachDetails(UUID id) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND));

        // 1. Lấy tất cả các Chuyến (Trip) mà chiếc xe này đang/đã thực hiện
        List<sp26.group.busticket.modules.entity.Trip> trips = tripRepository.findByCoach_Id(id);
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        List<TripDetailDTO> tripDetails = trips.stream()
                .map(trip -> {
                    // 2. Với mỗi chuyến, lấy danh sách vé (khách hàng)
                    List<Ticket> tickets = ticketRepository.findByBooking_Trip_Id(trip.getId());
                    
                    List<PassengerDetailDTO> passengers = tickets.stream()
                            .map(t -> PassengerDetailDTO.builder()
                                    .name(t.getPassengerName())
                                    .phone(t.getPassengerPhone())
                                    .seatNumber(t.getSeat().getSeatNumber())
                                    .ticketCode(t.getTicketCode())
                                    .tripInfo(trip.getRoute().getDepartureLocation().getName() + " -> " + 
                                              trip.getRoute().getArrivalLocation().getName())
                                    .build())
                            .collect(Collectors.toList());

                    return TripDetailDTO.builder()
                            .routeName(trip.getRoute().getDepartureLocation().getName() + " - " + 
                                       trip.getRoute().getArrivalLocation().getName())
                            .departureTime(trip.getDepartureTime().format(formatter))
                            .status(trip.getTripStatus().name())
                            .passengerCount(passengers.size())
                            .passengers(passengers)
                            .build();
                })
                .collect(Collectors.toList());

        return CoachDetailResponseDTO.builder()
                .id(coach.getId())
                .plateNumber(coach.getPlateNumber())
                .coachType(coach.getCoachType())
                .totalSeats(coach.getTotalSeats())
                .activeTrips(tripDetails)
                .build();
    }
}
