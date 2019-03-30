package com.sourcecode.malls.admin.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sourcecode.malls.admin.constants.SystemConstant;
import com.sourcecode.malls.admin.domain.Role;
import com.sourcecode.malls.admin.repository.jpa.impl.RoleRepository;

@Service
@Transactional
public class MerchantRoleService {
	@Autowired
	private RoleRepository repository;

	public void prepareMerchantUserRole() {
		Optional<Role> roleOp = repository.findByCode(SystemConstant.ROLE_MERCHANT_USER_CODE);
		if (!roleOp.isPresent()) {
			Role role = new Role();
			role.setCode(SystemConstant.ROLE_MERCHANT_USER_CODE);
			role.setName("商家用户");
			repository.save(role);
		}
	}

}
