package com.sourcecode.malls.admin.service.impl.merchant;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sourcecode.malls.admin.constants.SystemConstant;
import com.sourcecode.malls.admin.domain.system.setting.Authority;
import com.sourcecode.malls.admin.domain.system.setting.Role;
import com.sourcecode.malls.admin.repository.jpa.impl.system.AuthorityRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.system.RoleRepository;
import com.sourcecode.malls.admin.web.controller.AuthorityDefinitions;

@Service
@Transactional
public class MerchantRoleService {
	@Autowired
	private RoleRepository repository;

	@Autowired
	private AuthorityRepository authRepository;

	public void prepareMerchantUserRole() {
		Optional<Role> roleOp = repository.findByCode(SystemConstant.ROLE_MERCHANT_USER_CODE);
		Role role = null;
		if (!roleOp.isPresent()) {
			role = new Role();
			role.setCode(SystemConstant.ROLE_MERCHANT_USER_CODE);
			role.setName("商家用户");
			repository.save(role);
		} else {
			role = roleOp.get();
		}
		prepareMerchantUserAuthorities(role);
		repository.save(role);
	}

	private void prepareAuthority(Role role, AuthorityDefinitions definition) {
		Optional<Authority> authOp = authRepository.findByCode(definition.getCode());
		Authority auth = null;
		if (!authOp.isPresent()) {
			auth = new Authority();
			auth.setCode(definition.getCode());
			auth.setName(definition.getName());
			auth.setLink(definition.getLink());
			auth.setMethod(definition.getMethod());
			auth.addRole(role);
			authRepository.save(auth);
			role.addAuthority(auth);
		} else {
			auth = authOp.get();
			auth.setName(definition.getName());
			auth.setLink(definition.getLink());
			auth.setMethod(definition.getMethod());
			authRepository.save(auth);
		}
	}

	private void prepareMerchantUserAuthorities(Role role) {
		for (AuthorityDefinitions definition : AuthorityDefinitions.values()) {
			prepareAuthority(role, definition);
		}
	}

}
