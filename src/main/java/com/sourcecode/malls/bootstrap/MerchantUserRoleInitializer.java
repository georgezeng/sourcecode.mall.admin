package com.sourcecode.malls.bootstrap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sourcecode.malls.service.impl.merchant.MerchantRoleService;

@Component
public class MerchantUserRoleInitializer {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private MerchantRoleService service;

	@PostConstruct
	public void init() {
		try {
			service.prepareMerchantUserRole();
			service.prepareMerchantInitUserRole();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
