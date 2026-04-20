package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.coach.request.CoachRequestDTO;
import sp26.group.busticket.modules.dto.coach.response.CoachResponseDTO;
import sp26.group.busticket.modules.entity.Coach;
import sp26.group.busticket.modules.mapper.CoachMapper;
import sp26.group.busticket.modules.repository.CoachRepository;
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
        
        // Kiểm tra xem xe có đang nằm trong chuyến đi nào không
        if (tripRepository.existsByCoach_Id(id)) {
            throw new AppException(ErrorCode.INVALID_INPUT, 
                "Không thể xóa xe này vì đang có chuyến đi (Trip) liên quan. Vui lòng xóa chuyến đi trước!");
        }
        
        coachRepository.deleteById(id);
    }
}
