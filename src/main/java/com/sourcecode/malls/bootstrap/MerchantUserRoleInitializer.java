package com.sourcecode.malls.bootstrap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sourcecode.malls.service.impl.merchant.MerchantRoleService;

@Component
public class MerchantUserRoleInitializer {
	@Autowired
	private MerchantRoleService service;

	@PostConstruct
	public void init() {
		service.prepareMerchantUserRole();
	}
}