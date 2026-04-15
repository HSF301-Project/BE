package sp26.group.busticket.modules.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sp26.group.busticket.infrastructure.persistence.BaseEntity;

@Entity
@Table(name = "routes")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RouteEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_location_id", nullable = false)
    private LocationEntity departureLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_location_id", nullable = false)
    private LocationEntity arrivalLocation;

    @Column(nullable = false)
    private Float distance;

    @Column(nullable = false)
    private Integer duration; // Minutes
}
