package com.sourcecode.malls.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

import com.sourcecode.malls.web.security.filter.UserSessionFilter;

@Configuration
public class SecurityConfig extends BaseSecurityConfig {
	@Autowired
	private UserSessionFilter userSessionFilter;

	protected void after(HttpSecurity http) throws Exception {
		http.addFilterBefore(userSessionFilter, FilterSecurityInterceptor.class);
	}
}