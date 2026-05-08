package com.example.FastLane.Academy;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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