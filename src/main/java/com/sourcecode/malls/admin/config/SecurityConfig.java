package com.sourcecode.malls.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig extends BaseSecurityConfig {

	@Override
	protected void before(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/merchant/register/**").permitAll();
	}

	@Override
	protected void after(HttpSecurity http) throws Exception {

	}

}
