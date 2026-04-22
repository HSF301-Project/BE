package sp26.group.busticket.modules.dto.trip.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.modules.enumType.TripStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripRequestDTO {

    private UUID id;

    @NotNull(message = "Vui lòng chọn tuyến đường")
    private UUID routeId;

    @NotNull(message = "Vui lòng chọn xe")
    private UUID coachId;

    @NotNull(message = "Vui lòng chọn tài xế chính")
    private UUID driverId;

    private UUID secondDriverId;
    private UUID assistantId;

    @NotNull(message = "Thời gian khởi hành không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime departureTime;

    private Integer travelTimeHours;
    private Integer travelTimeMinutes;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Giá vé không được để trống")
    @Positive(message = "Giá vé phải lớn hơn 0")
    private BigDecimal priceBase;

    @NotBlank(message = "Số điện thoại liên hệ chuyến không được để trống")
    private String contactPhoneNumber;

    @NotNull(message = "Trạng thái chuyến đi không được để trống")
    @Builder.Default
    private TripStatusEnum status = TripStatusEnum.SCHEDULED;

    // --- Roundtrip Fields ---
    private boolean roundTrip = false;
    private UUID returnRouteId;
    private UUID returnDriverId;
    private UUID returnSecondDriverId;
    private UUID returnAssistantId;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime returnDepartureTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime returnArrivalTime;

    private BigDecimal priceRoundTrip;
}
