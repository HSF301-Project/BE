package sp26.group.busticket.dto.account.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.enumType.StatusEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffUpdateRequestDTO {

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    private String email;

    @NotBlank(message = "Ho ten khong duoc de trong")
    private String fullName;

    @NotBlank(message = "So dien thoai khong duoc de trong")
    private String phone;

    // Cap nhat mat khau la tuy chon
    private String password;

    private StatusEnum status;
}

