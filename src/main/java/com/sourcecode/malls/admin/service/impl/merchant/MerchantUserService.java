package com.sourcecode.malls.admin.service.impl.merchant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.druid.util.StringUtils;
import com.sourcecode.malls.admin.constants.ExceptionMessageConstant;
import com.sourcecode.malls.admin.constants.SystemConstant;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.system.setting.Authority;
import com.sourcecode.malls.admin.domain.system.setting.Role;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.admin.dto.merchant.MerchantDTO;
import com.sourcecode.malls.admin.dto.query.QueryInfo;
import com.sourcecode.malls.admin.dto.system.AuthorityDTO;
import com.sourcecode.malls.admin.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.system.AuthorityRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.system.RoleRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.system.UserRepository;
import com.sourcecode.malls.admin.service.base.JpaService;
import com.sourcecode.malls.admin.util.AssertUtil;
import com.sourcecode.malls.admin.util.RegexpUtil;
import com.sourcecode.malls.admin.web.controller.AuthorityDefinitions;

@Service
@Transactional
public class MerchantUserService implements JpaService<Merchant, Long> {

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

	public void register(User merchant) {
		AssertUtil.assertTrue(RegexpUtil.matchMobile(merchant.getUsername()), ExceptionMessageConstant.MOBILE_ACCOUNT_SHOULD_BE_THE_RULE);
		AssertUtil.assertTrue(RegexpUtil.matchPassword(merchant.getPassword()), ExceptionMessageConstant.PASSWORD_SHOULD_BE_THE_RULE);
		AssertUtil.assertTrue(merchant.getPassword().equals(merchant.getConfirmPassword()), ExceptionMessageConstant.TWO_TIMES_PASSWORD_NOT_EQUALS);
		Optional<User> existedUser = userRepository.findByUsername(merchant.getUsername());
		AssertUtil.assertTrue(!existedUser.isPresent(), "用户已存在");
		merchant.setEnabled(true);
		merchant.setPassword(pwdEncoder.encode(merchant.getPassword()));
		userRepository.save(merchant);
		Optional<Role> role = roleRepository.findByCode(SystemConstant.ROLE_MERCHANT_USER_CODE);
		AssertUtil.assertTrue(role.isPresent(), "商家角色不存在");
		role.get().addUser(merchant);
		roleRepository.save(role.get());
	}

	public Merchant createSubAccount(Merchant parent, MerchantDTO subAccount) {
		AssertUtil.assertTrue(RegexpUtil.matchMobile(subAccount.getUsername()), ExceptionMessageConstant.MOBILE_ACCOUNT_SHOULD_BE_THE_RULE);
		AssertUtil.assertTrue(RegexpUtil.matchPassword(subAccount.getPassword()), ExceptionMessageConstant.PASSWORD_SHOULD_BE_THE_RULE);
		AssertUtil.assertTrue(subAccount.getPassword().equals(subAccount.getConfirmPassword()),
				ExceptionMessageConstant.TWO_TIMES_PASSWORD_NOT_EQUALS);
		Optional<Merchant> existedUser = merchantRepository.findByUsername(subAccount.getUsername());
		AssertUtil.assertTrue(!existedUser.isPresent(), "用户已存在");
		Merchant data = subAccount.asEntity();
		data.setEnabled(true);
		data.setPassword(pwdEncoder.encode(subAccount.getPassword()));
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
			Optional<Authority> authOp = authRepository.findByCode(AuthorityDefinitions.MERCHANT_USER_PROFILE_PAGE.getCode());
			if (authOp.isPresent()) {
				subAccount.addAuthority(authOp.get().asDTO());
			}
		}
		for (AuthorityDTO authDTO : subAccount.getAuthorities()) {
			Authority auth = authRepository.findById(authDTO.getId()).get();
			auth.addRole(role);
			role.addAuthority(auth);
			authRepository.save(auth);
		}
		return data;
	}

	public Merchant updateSubAccount(Merchant parent, MerchantDTO subAccount) {
		Optional<Merchant> existedUser = merchantRepository.findByUsername(subAccount.getUsername());
		AssertUtil.assertTrue(existedUser.isPresent() && existedUser.get().isEnabled(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(existedUser.get().getParent() != null && existedUser.get().getParent().getId().equals(parent.getId()),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		if (!StringUtils.isEmpty(subAccount.getPassword())) {
			AssertUtil.assertTrue(RegexpUtil.matchPassword(subAccount.getPassword()), ExceptionMessageConstant.PASSWORD_SHOULD_BE_THE_RULE);
			AssertUtil.assertTrue(subAccount.getPassword().equals(subAccount.getConfirmPassword()),
					ExceptionMessageConstant.TWO_TIMES_PASSWORD_NOT_EQUALS);
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
		return existedUser.get();
	}

	@Transactional(readOnly = true)
	public Page<Merchant> findAllSubAccounts(Merchant parent, QueryInfo<SimpleQueryDTO> queryInfo) {
		SimpleQueryDTO data = queryInfo.getData();
		Page<Merchant> pageReulst = null;
		Specification<Merchant> spec = new Specification<Merchant>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Merchant> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("parent"), parent.getId()));
				if (data != null) {
					if (!StringUtils.isEmpty(data.getSearchText())) {
						String like = "%" + data.getSearchText() + "%";
						predicate.add(criteriaBuilder.or(criteriaBuilder.like(root.get("username").as(String.class), like),
								criteriaBuilder.like(root.get("email").as(String.class), like)));
					}
					if (!"all".equals(data.getStatusText())) {
						predicate.add(criteriaBuilder.equal(root.get("enabled").as(boolean.class), Boolean.valueOf(data.getStatusText())));
					}
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		pageReulst = merchantRepository.findAll(spec, queryInfo.getPage().pageable());
		return pageReulst;
	}

	public void resetPassword(User merchant) {
		AssertUtil.assertTrue(RegexpUtil.matchMobile(merchant.getUsername()), ExceptionMessageConstant.MOBILE_ACCOUNT_SHOULD_BE_THE_RULE);
		AssertUtil.assertTrue(RegexpUtil.matchPassword(merchant.getPassword()), ExceptionMessageConstant.PASSWORD_SHOULD_BE_THE_RULE);
		AssertUtil.assertTrue(merchant.getPassword().equals(merchant.getConfirmPassword()), ExceptionMessageConstant.TWO_TIMES_PASSWORD_NOT_EQUALS);
		Optional<User> existedUser = userRepository.findByUsername(merchant.getUsername());
		AssertUtil.assertTrue(existedUser.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		existedUser.get().setPassword(pwdEncoder.encode(merchant.getPassword()));
		userRepository.save(existedUser.get());
	}

	@Override
	public JpaRepository<Merchant, Long> getRepository() {
		return merchantRepository;
	}

}
