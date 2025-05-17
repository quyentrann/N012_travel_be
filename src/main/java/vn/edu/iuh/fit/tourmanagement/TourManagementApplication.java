package vn.edu.iuh.fit.tourmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TourManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(TourManagementApplication.class, args);
	}

}
