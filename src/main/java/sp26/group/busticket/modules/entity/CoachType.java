package sp26.group.busticket.modules.entity;

import jakarta.persistence.*;
import lombok.Data;
import sp26.group.busticket.infrastructure.persistence.BaseEntity;

@Entity
@Table(name = "coach_types")
public class CoachType extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String name;
    
    @Column(length = 255)
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
