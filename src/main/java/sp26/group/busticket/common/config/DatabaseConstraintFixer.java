package sp26.group.busticket.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DatabaseConstraintFixer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    @Transactional
    public void dropCoachStatusConstraint() {
        try {
            // Drop the old constraint that blocks INACTIVE status
            jdbcTemplate.execute("ALTER TABLE coaches DROP CONSTRAINT CK__coaches__status__529933DA");
            System.out.println("✅ SUCCESSFULLY DROPPED OLD STATUS CONSTRAINT FROM SQL SERVER!");
            
            // Add a new constraint that allows INACTIVE (Optional, SQL Server will just allow strings without it if not added back)
            jdbcTemplate.execute("ALTER TABLE coaches ADD CONSTRAINT CK_coaches_status_NEW CHECK (status IN ('AVAILABLE', 'WORKING', 'MAINTENANCE', 'INACTIVE'))");
            System.out.println("✅ SUCCESSFULLY ADDED NEW STATUS CONSTRAINT!");
        } catch (Exception e) {
            System.out.println("⚠️ Constraint might have already been dropped or generated a different error: " + e.getMessage());
        }
    }
}
