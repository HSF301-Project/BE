package sp26.group.busticket.modules.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import sp26.group.busticket.modules.enumType.CoachStatusEnum;
import sp26.group.busticket.infrastructure.persistence.BaseEntity;

@Entity
@Table(name = "coaches")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Coach extends BaseEntity {

    @Column(name = "plate_number", nullable = false, unique = true)
    private String plateNumber;

    @Column(name = "coach_type", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String coachType;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CoachStatusEnum status;
}
