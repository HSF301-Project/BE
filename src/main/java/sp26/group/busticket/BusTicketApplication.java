package sp26.group.busticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BusTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusTicketApplication.class, args);
    }

}
