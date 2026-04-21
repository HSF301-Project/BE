package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.coach.request.CoachRequestDTO;
import sp26.group.busticket.modules.dto.coach.response.CoachResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CoachService {
    CoachResponseDTO createCoach(CoachRequestDTO request);
    List<CoachResponseDTO> getAllCoaches();
    CoachResponseDTO getCoachById(UUID id);
    CoachResponseDTO updateCoach(UUID id, CoachRequestDTO request);
    void deleteCoach(UUID id);
    sp26.group.busticket.modules.dto.coach.response.CoachDetailResponseDTO getCoachDetails(UUID id);
    sp26.group.busticket.modules.dto.trip.response.AdminTripDetailResponseDTO getAdminTripDetail(UUID tripId);
}
