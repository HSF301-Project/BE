package sp26.group.busticket.dto.account.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String fullName;
    private String email;
    private String avatarUrl;
    private String membershipTier;
    private Integer totalTrips;
    private String membershipLabel;
}
