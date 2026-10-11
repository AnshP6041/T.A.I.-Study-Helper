package com.ansh.tai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaiApplication.class, args);
	}

}
