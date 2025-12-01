package com.ramaditya.webtask.webtask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;       

@SpringBootApplication
public class WebtaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebtaskApplication.class, args);
	}
	
	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate();
	}

}
