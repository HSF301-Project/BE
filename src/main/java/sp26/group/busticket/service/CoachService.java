package sp26.group.busticket.service;

import sp26.group.busticket.dto.coach.response.CoachDetailResponseDTO;
import sp26.group.busticket.dto.trip.response.AdminTripDetailResponseDTO;
import sp26.group.busticket.dto.coach.request.CoachRequestDTO;
import sp26.group.busticket.dto.coach.response.CoachResponseDTO;

import java.util.List;
import java.util.UUID;

import sp26.group.busticket.entity.CoachType;

public interface CoachService {
    CoachResponseDTO createCoach(CoachRequestDTO request);
    List<CoachResponseDTO> getAllCoaches();
    CoachResponseDTO getCoachById(UUID id);
    CoachResponseDTO updateCoach(UUID id, CoachRequestDTO request);
    void deleteCoach(UUID id);
    CoachDetailResponseDTO getCoachDetails(UUID id);
    AdminTripDetailResponseDTO getAdminTripDetail(UUID tripId);
    
    // CoachType methods
    List<CoachType> getAllCoachTypes();
    CoachType saveCoachType(CoachType coachType);
    void deleteCoachType(UUID id);
    long countCoachesByType(UUID typeId);
    CoachType getCoachTypeById(UUID id);
}
