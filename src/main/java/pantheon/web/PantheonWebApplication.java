package pantheon.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Web version: serves the browser UI and an API to create, run and watch simulations.
 */
@SpringBootApplication
public class PantheonWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(PantheonWebApplication.class, args);
	}
}
