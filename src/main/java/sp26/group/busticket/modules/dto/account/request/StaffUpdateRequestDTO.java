package sp26.group.busticket.modules.dto.account.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.modules.enumType.StatusEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffUpdateRequestDTO {

    @NotBlank(message = "Email khong duoc de trong")
    @Pattern(regexp = "^@.+\\.v$", message = "Email phai bat dau bang @ va ket thuc bang .vn")
    @Email(message = "Email khong hop le")
    private String email;

    @NotBlank(message = "Ho ten khong duoc de trong")
    @Pattern(regexp = "^.{10}$", message = "Ho ten phai co dung 10 ky tu")
    private String fullName;

    @NotBlank(message = "So dien thoai khong duoc de trong")
    @Pattern(regexp = "\\d{9}", message = "So dien thoai phai co dung 9 chu so")
    private String phone;

    // Cap nhat mat khau la tuy chon
    @NotBlank(message = "Mat khau khong duoc de trong")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{10}$",
            message = "Mat khau phai co dung 10 ky tu, bao gom chu va so"
    )
    private String password;

    private StatusEnum status;
}

