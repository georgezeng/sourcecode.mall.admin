package com.sourcecode.malls.admin.web.controller.merchant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.admin.constants.ExceptionMessageConstant;
import com.sourcecode.malls.admin.constants.SystemConstant;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.system.setting.Authority;
import com.sourcecode.malls.admin.domain.system.setting.Role;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.dto.base.KeyDTO;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.admin.dto.merchant.MerchantDTO;
import com.sourcecode.malls.admin.dto.query.PageResult;
import com.sourcecode.malls.admin.dto.query.QueryInfo;
import com.sourcecode.malls.admin.dto.system.AuthorityDTO;
import com.sourcecode.malls.admin.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.system.AuthorityRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.system.UserRepository;
import com.sourcecode.malls.admin.service.impl.merchant.MerchantUserService;
import com.sourcecode.malls.admin.util.AssertUtil;
import com.sourcecode.malls.admin.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/merchant/subAccount")
public class MerchantSubAccountController extends BaseController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private AuthorityRepository authRepository;

	@Autowired
	private MerchantUserService userService;

	@Value("${user.type.name}")
	private String userDir;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<MerchantDTO>> list(@RequestBody QueryInfo<SimpleQueryDTO> queryInfo) {
		User parentUser = getRelatedCurrentUser();
		Optional<Merchant> parent = merchantRepository.findById(parentUser.getId());
		Page<Merchant> pageResult = userService.findAllSubAccounts(parent.get(), queryInfo);
		PageResult<MerchantDTO> dtoResult = new PageResult<>(pageResult.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()),
				pageResult.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(value = "/load/params/{id}")
	public ResultBean<MerchantDTO> load(@PathVariable Long id) {
		Optional<Merchant> dataOp = merchantRepository.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
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
		User parentUser = getRelatedCurrentUser();
		Optional<Merchant> parent = merchantRepository.findById(parentUser.getId());
		Merchant data = null;
		if (merchant.getId() == null) {
			data = userService.createSubAccount(parent.get(), merchant);
		} else {
			data = userService.updateSubAccount(parent.get(), merchant);
		}
		List<String> tmpPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		if (merchant.getAvatar() != null && merchant.getAvatar().startsWith("temp")) {
			String newPath = userDir + "/" + data.getId() + "/avatar.png";
			newPaths.add(newPath);
			tmpPaths.add(merchant.getAvatar());
			data.setAvatar(newPath);
		}
		merchantRepository.save(data);
		transfer(false, tmpPaths, newPaths);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		User parentUser = getRelatedCurrentUser();
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
	public ResultBean<AuthorityDTO> authorities() {
		List<Authority> auths = authRepository.findAllWithoutSuperAdmin();
		return new ResultBean<>(auths.stream().map(auth -> auth.asDTO()));
	}

	@RequestMapping(value = "/updateStatus/params/{status}")
	public ResultBean<Void> updateStatus(@RequestBody KeyDTO<Long> keys, @PathVariable Boolean status) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_UPDATE);
		for (Long id : keys.getIds()) {
			Optional<Merchant> userOp = userService.findById(id);
			if (userOp.isPresent() && userOp.get().getParent() != null && userOp.get().getParent().getId().equals(getRelatedCurrentUser().getId())) {
				Merchant user = userOp.get();
				user.setEnabled(status);
				userService.save(user);
			}
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/file/upload/params/{id}")
	public ResultBean<String> upload(@RequestParam("file") MultipartFile file, @PathVariable Long id) throws IOException {
		return upload(file, userDir, null, id, false);
	}

	@RequestMapping(value = "/file/load/params/{id}")
	public Resource load(@RequestParam String filePath, @PathVariable Long id) {
		return load(id, filePath, userDir, false);
	}

}
