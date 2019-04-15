package com.sourcecode.malls.admin.schedule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.sourcecode.malls.admin.constants.SystemConstant;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.system.setting.Authority;
import com.sourcecode.malls.admin.domain.system.setting.Role;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.repository.jpa.impl.AuthorityRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.UserRepository;

//@Component
public class SubAuthoritySyncJob {

	@Autowired
	private AuthorityRepository authRepository;

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private UserRepository userRepository;

	@Scheduled(cron = "${schedule.cron.sub-auth-sync-job}")
	@Transactional
	public void run() {
		List<Merchant> subAccounts = merchantRepository.findAllByParentIsNotNull();
		if (!CollectionUtils.isEmpty(subAccounts)) {
			List<Authority> auths = authRepository.findAllForSubAccount();
			if (!CollectionUtils.isEmpty(auths)) {
				subAccounts.parallelStream().forEach(sub -> {
					User user = userRepository.findById(sub.getId()).get();
					Role subRole = null;
					for (Role role : user.getRoles()) {
						if (role.getCode().startsWith(SystemConstant.ROLE_MERCHANT_SUB_ACCOUNT_CODE)) {
							subRole = role;
							break;
						}
					}
					Role theSubRole = subRole;
					auths.parallelStream().forEach(auth -> {
						boolean found = false;
						if (!CollectionUtils.isEmpty(auth.getRoles())) {
							for (Role role : auth.getRoles()) {
								if (role.getId().equals(theSubRole.getId())) {
									found = true;
									break;
								}
							}
						}
						if (!found) {
							auth.addRole(theSubRole);
							// theSubRole.addAuthority(auth);
							authRepository.save(auth);
						}
					});
				});
			}
		}
	}
}
