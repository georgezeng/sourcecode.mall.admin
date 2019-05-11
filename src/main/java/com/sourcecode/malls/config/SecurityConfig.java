package com.sourcecode.malls.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig extends BaseSecurityConfig {

	@Override
	protected void before(HttpSecurity http) throws Exception {
		
	}

	@Override
	protected void after(HttpSecurity http) throws Exception {

	}

}
