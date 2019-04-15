package com.sourcecode.malls.admin.service.impl;

import java.util.HashSet;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.druid.util.StringUtils;
import com.sourcecode.malls.admin.constants.SystemConstant;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.system.setting.Authority;
import com.sourcecode.malls.admin.domain.system.setting.Role;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.dto.merchant.MerchantDTO;
import com.sourcecode.malls.admin.dto.query.QueryInfo;
import com.sourcecode.malls.admin.dto.system.setting.AuthorityDTO;
import com.sourcecode.malls.admin.properties.UserProperties;
import com.sourcecode.malls.admin.repository.jpa.impl.AuthorityRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.RoleRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.UserRepository;
import com.sourcecode.malls.admin.util.AssertUtil;
import com.sourcecode.malls.admin.util.RegexpUtil;

@Service
@Transactional
public class MerchantUserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private AuthorityRepository authRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder pwdEncoder;

	@Autowired
	private UserProperties userProperties;

	public void register(User merchant) {
		AssertUtil.assertTrue(RegexpUtil.matchMobile(merchant.getUsername()), "账号长度必须是11位且全部是数字");
		AssertUtil.assertTrue(RegexpUtil.matchPassword(merchant.getPassword()), "密码必须数字+字母（区分大小写）并且不少于8位");
		AssertUtil.assertTrue(merchant.getPassword().equals(merchant.getConfirmPassword()), "确认密码与密码不一致");
		Optional<User> existedUser = userRepository.findByUsername(merchant.getUsername());
		AssertUtil.assertTrue(!existedUser.isPresent(), "用户已存在");
		merchant.setEnabled(true);
		merchant.setPassword(pwdEncoder.encode(merchant.getPassword()));
		merchant.setHeader(userProperties.getAvatar());
		userRepository.save(merchant);
		Optional<Role> role = roleRepository.findByCode(SystemConstant.ROLE_MERCHANT_USER_CODE);
		AssertUtil.assertTrue(role.isPresent(), "商家角色不存在");
		role.get().addUser(merchant);
		roleRepository.save(role.get());
	}

	public void createSubAccount(Merchant parent, MerchantDTO subAccount) {
		AssertUtil.assertTrue(RegexpUtil.matchMobile(subAccount.getUsername()), "账号长度必须是11位且全部是数字");
		AssertUtil.assertTrue(RegexpUtil.matchPassword(subAccount.getPassword()), "密码必须数字+字母（区分大小写）并且不少于8位");
		AssertUtil.assertTrue(subAccount.getPassword().equals(subAccount.getConfirmPassword()), "确认密码与密码不一致");
		Optional<Merchant> existedUser = merchantRepository.findByUsername(subAccount.getUsername());
		AssertUtil.assertTrue(!existedUser.isPresent(), "用户已存在");
		Merchant data = subAccount.asEntity();
		data.setEnabled(true);
		data.setPassword(pwdEncoder.encode(subAccount.getPassword()));
		data.setHeader(userProperties.getAvatar());
		data.setParent(parent);
		merchantRepository.save(data);
		Optional<User> user = userRepository.findById(data.getId());
		Role role = new Role();
		role.setCode(SystemConstant.ROLE_MERCHANT_SUB_ACCOUNT_CODE + "_" + subAccount.getUsername());
		role.setName("子账户角色-" + subAccount.getUsername());
		role.addUser(user.get());
		role.setHidden(true);
		roleRepository.save(role);
		if (CollectionUtils.isEmpty(subAccount.getAuthorities())) {
			for (AuthorityDTO authDTO : subAccount.getAuthorities()) {
				Authority auth = authRepository.findById(authDTO.getId()).get();
				auth.addRole(role);
				role.addAuthority(auth);
				authRepository.save(auth);
			}
		}
	}

	public void updateSubAccount(Merchant parent, MerchantDTO subAccount) {
		Optional<Merchant> existedUser = merchantRepository.findByUsername(subAccount.getUsername());
		AssertUtil.assertTrue(existedUser.isPresent() && existedUser.get().isEnabled(), "用户不存在");
		AssertUtil.assertTrue(existedUser.get().getParent() != null && existedUser.get().getParent().getId().equals(parent.getId()), "用户不存在");
		if (!StringUtils.isEmpty(subAccount.getPassword())) {
			AssertUtil.assertTrue(RegexpUtil.matchPassword(subAccount.getPassword()), "密码必须数字+字母（区分大小写）并且不少于8位");
			AssertUtil.assertTrue(subAccount.getPassword().equals(subAccount.getConfirmPassword()), "确认密码与密码不一致");
			existedUser.get().setPassword(pwdEncoder.encode(subAccount.getPassword()));
		}
		BeanUtils.copyProperties(subAccount, existedUser.get(), "id", "parent", "password");
		merchantRepository.save(existedUser.get());
		User user = userRepository.findById(existedUser.get().getId()).get();
		Role role = null;
		for (Role r : user.getRoles()) {
			if (r.getCode().startsWith(SystemConstant.ROLE_MERCHANT_SUB_ACCOUNT_CODE)) {
				role = r;
				break;
			}
		}
		role.setAuthorities(new HashSet<>());
		if (!CollectionUtils.isEmpty(subAccount.getAuthorities())) {
			for (AuthorityDTO authDTO : subAccount.getAuthorities()) {
				Authority auth = authRepository.findById(authDTO.getId()).get();
				boolean found = false;
				if (!CollectionUtils.isEmpty(auth.getRoles())) {
					for (Role r : auth.getRoles()) {
						if (r.getId().equals(role.getId())) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					auth.addRole(role);
					authRepository.save(auth);
				}
				role.addAuthority(auth);
			}
		}
		roleRepository.save(role);
	}

	@Transactional(readOnly = true)
	public Page<Merchant> findAllSubAccounts(Merchant parent, QueryInfo<String> queryInfo) {
		String searchText = queryInfo.getData();
		Page<Merchant> pageReulst = null;
		if (!StringUtils.isEmpty(searchText)) {
			String like = "%" + searchText + "%";
			pageReulst = merchantRepository.findAllByParentAndEnabledAndUsernameLike(parent, true, like, queryInfo.getPage().pageable());
		} else {
			pageReulst = merchantRepository.findAllByParentAndEnabled(parent, true, queryInfo.getPage().pageable());
		}
		return pageReulst;
	}

	public void resetPassword(User merchant) {
		AssertUtil.assertTrue(RegexpUtil.matchMobile(merchant.getUsername()), "账号长度必须是11位且全部是数字");
		AssertUtil.assertTrue(RegexpUtil.matchPassword(merchant.getPassword()), "密码必须数字+字母（区分大小写）并且不少于8位");
		AssertUtil.assertTrue(merchant.getPassword().equals(merchant.getConfirmPassword()), "确认密码与密码不一致");
		Optional<User> existedUser = userRepository.findByUsername(merchant.getUsername());
		AssertUtil.assertTrue(existedUser.isPresent(), "用户不存在");
		existedUser.get().setPassword(pwdEncoder.encode(merchant.getPassword()));
		userRepository.save(existedUser.get());
	}

}
