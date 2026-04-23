package sp26.group.busticket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sp26.group.busticket.dto.coach.request.CoachRequestDTO;
import sp26.group.busticket.dto.coach.response.CoachResponseDTO;
import sp26.group.busticket.entity.Coach;

@Mapper(componentModel = "spring")
public interface CoachMapper {
    @org.mapstruct.Mapping(target = "coachType", ignore = true)
    Coach toEntity(CoachRequestDTO request);

    @org.mapstruct.Mapping(target = "coachType", source = "coachType.name")
    @org.mapstruct.Mapping(target = "coachTypeId", source = "coachType.id")
    CoachResponseDTO toResponse(Coach coach);

    @org.mapstruct.Mapping(target = "coachType", ignore = true)
    void updateEntity(@MappingTarget Coach coach, CoachRequestDTO request);
}
