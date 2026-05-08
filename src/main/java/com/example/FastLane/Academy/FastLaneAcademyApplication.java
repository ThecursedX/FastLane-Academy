package com.example.FastLane.Academy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FastLaneAcademyApplication {

	public static void main(String[] args) {
		SpringApplication.run(FastLaneAcademyApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}
}

//initial commit
