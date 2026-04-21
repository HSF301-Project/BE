package sp26.group.busticket.modules.dto.account.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.modules.enumType.StatusEnum;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDTO {
    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private StatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
