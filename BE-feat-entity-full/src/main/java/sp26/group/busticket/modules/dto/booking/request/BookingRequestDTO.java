package sp26.group.busticket.modules.dto.booking.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingRequestDTO {
    
    @NotNull(message = "Trip ID is required")
    private Integer tripId;
    
    @NotEmpty(message = "At least one passenger is required")
    private List<PassengerInfoDTO> passengers;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    @Data
    public static class PassengerInfoDTO {
        @NotNull(message = "Seat ID is required")
        private Integer seatId;
        
        @NotBlank(message = "Passenger name is required")
        private String passengerName;
    }
}
