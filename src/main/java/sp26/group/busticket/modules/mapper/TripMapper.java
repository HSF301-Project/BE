package sp26.group.busticket.modules.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sp26.group.busticket.modules.dto.trip.response.TripBookingResponseDTO;
import sp26.group.busticket.modules.dto.trip.response.TripResponseDTO;
import sp26.group.busticket.modules.entity.Trip;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "departureStation", source = "route.departureLocation.name")
    @Mapping(target = "arrivalStation", source = "route.arrivalLocation.name")
    @Mapping(target = "departureDateTimeLabel", ignore = true)
    @Mapping(target = "arrivalDateTimeLabel", ignore = true)
    TripBookingResponseDTO toTripBookingResponseDTO(Trip trip);

    @Mapping(target = "name", expression = "java(\"Chuyến xe \" + trip.getCoach().getPlateNumber())")
    @Mapping(target = "busTypeLabel", source = "coach.coachType")
    @Mapping(target = "departureStation", source = "route.departureLocation.name")
    @Mapping(target = "arrivalStation", source = "route.arrivalLocation.name")
    @Mapping(target = "seatsLeft", source = "coach.totalSeats")
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "featured", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "price", source = "priceBase")
    @Mapping(target = "priceFormatted", ignore = true)
    @Mapping(target = "departureTime", ignore = true)
    @Mapping(target = "arrivalTime", ignore = true)
    @Mapping(target = "duration", ignore = true)
    TripResponseDTO toTripResponseDTO(Trip trip);
}
