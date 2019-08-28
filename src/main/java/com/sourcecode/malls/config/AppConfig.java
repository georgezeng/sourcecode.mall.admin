package com.sourcecode.malls.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sourcecode.malls.web.security.filter.UserSessionFilter;

@Configuration
public class AppConfig {
	@Bean
	public UserSessionFilter userSessionFilter() {
		return new UserSessionFilter();
	}
}
