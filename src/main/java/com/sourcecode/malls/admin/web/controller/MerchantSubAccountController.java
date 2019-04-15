package com.sourcecode.malls.admin.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.admin.constants.SystemConstant;
import com.sourcecode.malls.admin.context.UserContext;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.system.setting.Authority;
import com.sourcecode.malls.admin.domain.system.setting.Role;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.dto.base.KeyDTO;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.dto.merchant.MerchantDTO;
import com.sourcecode.malls.admin.dto.query.PageResult;
import com.sourcecode.malls.admin.dto.query.QueryInfo;
import com.sourcecode.malls.admin.dto.system.setting.AuthorityDTO;
import com.sourcecode.malls.admin.repository.jpa.impl.AuthorityRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.UserRepository;
import com.sourcecode.malls.admin.service.impl.MerchantUserService;
import com.sourcecode.malls.admin.util.AssertUtil;

@RestController
@RequestMapping(path = "/merchant/subAccount")
public class MerchantSubAccountController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private AuthorityRepository authRepository;

	@Autowired
	private MerchantUserService userService;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<MerchantDTO>> list(@RequestBody QueryInfo<String> queryInfo) {
		User parentUser = UserContext.get();
		Optional<Merchant> parent = merchantRepository.findById(parentUser.getId());
		Page<Merchant> pageResult = userService.findAllSubAccounts(parent.get(), queryInfo);
		PageResult<MerchantDTO> dtoResult = new PageResult<>();
		if (pageResult.hasContent()) {
			dtoResult = new PageResult<>(pageResult.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()),
					pageResult.getTotalElements());
		}
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(value = "/one/params/{id}")
	public ResultBean<MerchantDTO> findOne(@PathVariable Long id) {
		Optional<Merchant> dataOp = merchantRepository.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), "查找不到相应的记录");
		Optional<User> user = userRepository.findById(id);
		List<AuthorityDTO> authorities = new ArrayList<>();
		for (Role role : user.get().getRoles()) {
			if (role.getCode().startsWith(SystemConstant.ROLE_MERCHANT_SUB_ACCOUNT_CODE)) {
				authorities.addAll(role.getAuthorities().stream().map(auth -> auth.asDTO()).collect(Collectors.toList()));
				break;
			}
		}
		MerchantDTO dto = dataOp.get().asDTO();
		dto.setAuthorities(authorities);
		return new ResultBean<>(dto);
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody MerchantDTO merchant) {
		User parentUser = UserContext.get();
		Optional<Merchant> parent = merchantRepository.findById(parentUser.getId());
		if (merchant.getId() == null) {
			userService.createSubAccount(parent.get(), merchant);
		} else {
			userService.updateSubAccount(parent.get(), merchant);
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), "必须选择至少一条记录进行删除");
		User parentUser = UserContext.get();
		for (Long id : keys.getIds()) {
			Optional<Merchant> userOp = merchantRepository.findById(id);
			if (userOp.isPresent() && userOp.get().getParent() != null && userOp.get().getParent().getId().equals(parentUser.getId())) {
				userOp.get().setEnabled(false);
				merchantRepository.save(userOp.get());
			}
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/authorities")
	public ResultBean<PageResult<AuthorityDTO>> authorities(@RequestBody QueryInfo<Void> queryInfo) {
		Page<Authority> auths = authRepository.findAllForSubAccount(queryInfo.getPage().pageable());
		return new ResultBean<>(
				new PageResult<>(auths.getContent().stream().map(auth -> auth.asDTO()).collect(Collectors.toList()), auths.getTotalElements()));
	}

}
