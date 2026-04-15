package sp26.group.busticket.modules.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sp26.group.busticket.modules.dto.auth.request.RegisterRequestDTO;
import sp26.group.busticket.modules.entity.AccountEntity;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true) // Sẽ encode thủ công
    AccountEntity toAccountEntity(RegisterRequestDTO dto);
}
