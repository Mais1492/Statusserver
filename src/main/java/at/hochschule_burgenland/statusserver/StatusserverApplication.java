package at.hochschule_burgenland.statusserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StatusserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(StatusserverApplication.class, args);
	}

}
