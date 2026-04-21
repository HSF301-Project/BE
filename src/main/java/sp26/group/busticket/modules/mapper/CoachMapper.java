package sp26.group.busticket.modules.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sp26.group.busticket.modules.dto.coach.request.CoachRequestDTO;
import sp26.group.busticket.modules.dto.coach.response.CoachResponseDTO;
import sp26.group.busticket.modules.entity.Coach;

@Mapper(componentModel = "spring")
public interface CoachMapper {
    Coach toEntity(CoachRequestDTO request);

    CoachResponseDTO toResponse(Coach coach);

    void updateEntity(@MappingTarget Coach coach, CoachRequestDTO request);
}
