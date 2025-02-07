package com.springsecurityexam.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final CustomAuthenticationFilter customAuthenticationFilter;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, CustomAuthenticationFilter customAuthenticationFilter) throws Exception {
		http
			.authorizeHttpRequests((authorizeHttpRequests) ->
				authorizeHttpRequests
					.requestMatchers("/h2-console/**")
					.permitAll()
					.requestMatchers(HttpMethod.GET, "/api/*/posts/{id:\\d+}", "/api/*/posts",
						"/api/*/posts/{postId:\\d+}/comments")
					.permitAll()
					.requestMatchers("/api/*/members/login", "/api/*/members/join")
					.permitAll()
					.anyRequest()
					.authenticated()
			)
			.headers((headers) -> headers
				.addHeaderWriter(new XFrameOptionsHeaderWriter(
					XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
			.csrf((csrf) -> csrf.disable())
			.addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
