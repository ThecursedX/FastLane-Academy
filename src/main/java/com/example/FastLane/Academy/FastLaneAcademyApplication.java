package com.example.FastLane.Academy;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
public class FastLaneAcademyApplication {

	public static void main(String[] args) {
		SpringApplication.run(FastLaneAcademyApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	@Configuration
	public class SecurityConfig {

		@Bean
		public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

			http.csrf(csrf -> csrf.disable())
					.authorizeHttpRequests(auth -> auth
							.anyRequest().permitAll()
					);

			return http.build();
		}
	}
}

//initial commit
