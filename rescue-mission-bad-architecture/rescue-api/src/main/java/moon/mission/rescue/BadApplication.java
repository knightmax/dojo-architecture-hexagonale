package moon.mission.rescue;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class BadApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(
			BadApplication.class).run(args);
	}
}
