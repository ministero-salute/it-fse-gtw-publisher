package it.finanze.sanita.fse2.ms.gtwpublisher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class GtwPublisherMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GtwPublisherMsApplication.class, args);
	}

	/**
	 * Definizione rest template.
	 *
	 * @return	rest template
	 */
	@Bean
	@Qualifier("restTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
