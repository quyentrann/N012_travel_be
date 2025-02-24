package vn.edu.iuh.fit.tourmanagement;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

@SpringBootTest
class TourManagementApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
		Faker faker = new Faker();
		if (userRepository.count() == 0) {
			IntStream.range(0, 100)  // Seed 100 người dùng
					.forEach(i -> {
						User user = User.builder()
								.code(faker.code().asin())
								.userName(faker.name().username())
								.password(faker.internet().password())
								.email(faker.internet().emailAddress())
								.phoneNumber(faker.phoneNumber().cellPhone())
								.fullName(faker.name().fullName())
								.address(faker.address().fullAddress())
								.dateOfBirth(LocalDateTime.now())
								.build();

						userRepository.save(user);
					});
			System.out.println("Seeded 100 users.");
		}

	}

}
