package sp26.group.busticket.modules.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
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

    @ManyToOne
    @JoinColumn(name = "coach_type_id", nullable = false)
    private CoachType coachType;

    public CoachType getCoachType() {
        return coachType;
    }

    public void setCoachType(CoachType coachType) {
        this.coachType = coachType;
    }

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CoachStatusEnum status;
}
