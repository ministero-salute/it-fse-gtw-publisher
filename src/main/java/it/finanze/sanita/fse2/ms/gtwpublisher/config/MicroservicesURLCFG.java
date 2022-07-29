package it.finanze.sanita.fse2.ms.gtwpublisher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 *  Microservices URL.
 */
@Configuration
@Getter
public class MicroservicesURLCFG {

	/**
	 *  Validator host.
	 */
	@Value("${ms.url.gtw-eds-client-service}")
	private String edsClientHost;

	@Value("${ms.url.gtw-eds-client-path}")
	private String edsClientPath;

	@Value("${ms.url.gtw-eds-client.publish.ep}")
	private String edsClientPublish;

}
