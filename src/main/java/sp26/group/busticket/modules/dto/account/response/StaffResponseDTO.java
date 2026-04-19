package sp26.group.busticket.modules.dto.account.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.modules.enumType.StatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponseDTO {
    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private StatusEnum status;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

