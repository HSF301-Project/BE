package sp26.group.busticket.modules.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^@.+\\.v$", message = "Email phai bat dau bang @ va ket thuc bang .vn")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{10}$",
            message = "Mat khau phai co dung 10 ky tu, bao gom chu va so"
    )
    private String password;
}
