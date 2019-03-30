package com.sourcecode.malls.admin.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sourcecode.malls.admin.constants.SystemConstant;
import com.sourcecode.malls.admin.domain.Role;
import com.sourcecode.malls.admin.domain.User;
import com.sourcecode.malls.admin.properties.UserProperties;
import com.sourcecode.malls.admin.repository.jpa.impl.RoleRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.UserRepository;
import com.sourcecode.malls.admin.util.AssertUtil;
import com.sourcecode.malls.admin.util.RegexpUtil;

@Service
@Transactional
public class MerchantUserService {
	@Autowired
	private UserRepository repository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder pwdEncoder;

	@Autowired
	private UserProperties userProperties;

	public void register(User merchant) {
		AssertUtil.assertNotNull(merchant, "没有提交用户数据");
		AssertUtil.assertTrue(!StringUtils.isEmpty(merchant.getUsername()), "账号不能为空");
		AssertUtil.assertTrue(RegexpUtil.matchMobile(merchant.getUsername()), "账号长度必须是11位且全部是数字");
		AssertUtil.assertTrue(!StringUtils.isEmpty(merchant.getEmail()), "邮箱不能为空");
		AssertUtil.assertTrue(RegexpUtil.matchEmail(merchant.getEmail()), "邮箱格式不正确");
		AssertUtil.assertTrue(!StringUtils.isEmpty(merchant.getPassword()), "密码不能为空");
		AssertUtil.assertTrue(RegexpUtil.matchPassword(merchant.getPassword()), "密码必须数字+字母（区分大小写）并且不少于8位");
		AssertUtil.assertTrue(merchant.getPassword().equals(merchant.getConfirmPassword()), "确认密码与密码不一致");
		Optional<User> existedUser = repository.findByUsername(merchant.getUsername());
		AssertUtil.assertTrue(!existedUser.isPresent(), "用户已存在");
		merchant.setEnabled(true);
		merchant.setPassword(pwdEncoder.encode(merchant.getPassword()));
		merchant.setHeader(userProperties.getAvatar());
		repository.save(merchant);
		Optional<Role> role = roleRepository.findByCode(SystemConstant.ROLE_MERCHANT_USER_CODE);
		AssertUtil.assertTrue(role.isPresent(), "商家角色不存在");
		role.get().addUser(merchant);
		roleRepository.save(role.get());
	}

}
