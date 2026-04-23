package sp26.group.busticket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sp26.group.busticket.dto.account.request.StaffCreateRequestDTO;
import sp26.group.busticket.dto.account.request.StaffUpdateRequestDTO;
import sp26.group.busticket.dto.account.response.StaffResponseDTO;
import sp26.group.busticket.entity.Account;

@Mapper(componentModel = "spring")
public interface StaffMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    Account toAccount(StaffCreateRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateAccountFromDto(StaffUpdateRequestDTO dto, @MappingTarget Account account);

    StaffResponseDTO toStaffResponseDTO(Account account);
}

