package sp26.group.busticket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sp26.group.busticket.dto.auth.request.RegisterRequestDTO;
import sp26.group.busticket.entity.Account;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true) // Sẽ encode thủ công
    Account toAccount(RegisterRequestDTO dto);
}
