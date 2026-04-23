package sp26.group.busticket.dto.finance.response;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class TransactionResponseDTO {
    private UUID id;
    private String txnId;
    private String bookingCode;
    private String amountFormatted;
    private String paymentMethod;
    private String paymentBadge;
    private String paymentLabel;
    private String status;
    private String statusLabel;
    private String dateTimeLabel;
}
