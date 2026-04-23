package sp26.group.busticket.modules.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sp26.group.busticket.modules.dto.trip.response.AdminTripDetailResponseDTO;
import sp26.group.busticket.modules.dto.trip.response.TripBookingResponseDTO;
import sp26.group.busticket.modules.dto.trip.response.TripResponseDTO;
import sp26.group.busticket.modules.entity.CoachType;
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
    @Mapping(target = "driverName", source = "driver.fullName")
    @Mapping(target = "secondDriverName", source = "secondDriver.fullName")
    TripResponseDTO toTripResponseDTO(Trip trip);

    @Mapping(target = "tripId", source = "id")
    @Mapping(target = "routeName", expression = "java(trip.getRoute().getDepartureLocation().getCity() + \" - \" + trip.getRoute().getArrivalLocation().getCity())")
    @Mapping(target = "departureTime", expression = "java(trip.getDepartureTime().format(java.time.format.DateTimeFormatter.ofPattern(\"HH:mm\")))")
    @Mapping(target = "arrivalTime", expression = "java(trip.getArrivalTime().format(java.time.format.DateTimeFormatter.ofPattern(\"HH:mm\")))")
    @Mapping(target = "departureDate", expression = "java(trip.getDepartureTime().format(java.time.format.DateTimeFormatter.ofPattern(\"dd/MM/yyyy\")))")
    @Mapping(target = "coachPlate", source = "coach.plateNumber")
    @Mapping(target = "coachType", source = "coach.coachType")
    @Mapping(target = "driverName", source = "driver.fullName")
    @Mapping(target = "driverPhone", source = "driver.phone")
    @Mapping(target = "secondDriverName", source = "secondDriver.fullName")
    @Mapping(target = "secondDriverPhone", source = "secondDriver.phone")
    @Mapping(target = "assistantName", source = "assistant.fullName")
    @Mapping(target = "assistantPhone", source = "assistant.phone")
    @Mapping(target = "pickUpAddress", source = "route.departureLocation.address")
    @Mapping(target = "dropOffAddress", source = "route.arrivalLocation.address")
    @Mapping(target = "intermediateStopsText", ignore = true)
    @Mapping(target = "stopEtas", ignore = true)
    @Mapping(target = "seats", ignore = true)
    AdminTripDetailResponseDTO toAdminTripDetailResponseDTO(Trip trip);

    default String mapCoachType(CoachType coachType) {
        return coachType != null ? coachType.getName() : null;
    }
}
