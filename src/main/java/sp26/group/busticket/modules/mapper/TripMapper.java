package sp26.group.busticket.modules.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sp26.group.busticket.modules.dto.trip.TripAdminResponseDTO;
import sp26.group.busticket.modules.dto.trip.response.TripBookingResponseDTO;
import sp26.group.busticket.modules.entity.Trip;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "departureStation", source = "route.departureLocation.name")
    @Mapping(target = "arrivalStation", source = "route.arrivalLocation.name")
    @Mapping(target = "departureDateTimeLabel", ignore = true)
    @Mapping(target = "arrivalDateTimeLabel", ignore = true)
    TripBookingResponseDTO toTripBookingResponseDTO(Trip trip);

    @Mapping(target = "name", ignore = true)
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
    @Mapping(target = "departureAmPm", ignore = true)
    @Mapping(target = "arrivalTime", ignore = true)
    @Mapping(target = "duration", ignore = true)
    sp26.group.busticket.modules.dto.trip.response.TripResponseDTO toClientTripResponseDTO(Trip trip);

    @Mapping(target = "tripCode", ignore = true)
    @Mapping(target = "fromStation", source = "route.departureLocation.name")
    @Mapping(target = "toStation", source = "route.arrivalLocation.name")
    @Mapping(target = "fromCity", source = "route.departureLocation.city")
    @Mapping(target = "toCity", source = "route.arrivalLocation.city")
    @Mapping(target = "busType", source = "coach.coachType")
    @Mapping(target = "busTypeLabel", source = "coach.coachType")
    @Mapping(target = "departureTime", ignore = true)
    @Mapping(target = "departureAmPm", ignore = true)
    @Mapping(target = "departureDateTime", ignore = true)
    @Mapping(target = "arrivalDateTime", ignore = true)
    @Mapping(target = "arrivalTime", ignore = true)
    @Mapping(target = "driverName", source = "driver.fullName")
    @Mapping(target = "driverPhone", source = "driver.phone")
    @Mapping(target = "assistantName", source = "assistant.fullName")
    @Mapping(target = "assistantPhone", source = "assistant.phone")
    @Mapping(target = "coachPlate", source = "coach.plateNumber")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "statusLabel", ignore = true)
    @Mapping(target = "bookedSeats", ignore = true)
    @Mapping(target = "totalSeats", ignore = true)
    @Mapping(target = "fillPercent", ignore = true)
    @Mapping(target = "priceFormatted", ignore = true)
    @Mapping(target = "routeTimeline", ignore = true)
    @Mapping(target = "nextStopLabel", ignore = true)
    @Mapping(target = "hasIssue", ignore = true)
    @Mapping(target = "minutesUntilDeparture", ignore = true)
    @Mapping(target = "formattedDepartureCountdown", ignore = true)
    TripAdminResponseDTO toAdminTripResponseDTO(Trip trip);
}
