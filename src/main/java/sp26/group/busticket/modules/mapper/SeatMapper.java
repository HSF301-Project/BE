package sp26.group.busticket.modules.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sp26.group.busticket.modules.dto.booking.response.SeatDisplayDTO;
import sp26.group.busticket.modules.entity.Seat;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(target = "seatId", source = "seatNumber")
    @Mapping(target = "status", ignore = true) // Will be set manually based on booking status
    @Mapping(target = "aisleAfter", ignore = true) // Will be set manually
    SeatDisplayDTO toSeatDisplayDTO(Seat seat);
}
