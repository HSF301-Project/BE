package sp26.group.busticket.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "coach_types")
public class CoachType extends BaseEntity {

    @Column(unique = true, nullable = false, columnDefinition = "NVARCHAR(100)")
    private String name;
    
    @Column(columnDefinition = "NVARCHAR(255)")
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
