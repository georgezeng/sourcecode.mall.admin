package com.sourcecode.malls.web.controller.merchant;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.domain.redis.CodeStore;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.repository.redis.impl.CodeStoreRepository;
import com.sourcecode.malls.service.impl.VerifyCodeService;
import com.sourcecode.malls.service.impl.merchant.MerchantUserService;
import com.sourcecode.malls.util.AssertUtil;

@RestController
@RequestMapping(path = "/merchant")
public class MerchantUserController {
	private static final String REGISTER_CODE_CATEGORY = "merchant-register-code";
	private static final String FORGET_PASSWORD_CODE_CATEGORY = "merchant-forget-password-code";

	@Autowired
	private MerchantRepository repository;

	@Autowired
	private MerchantUserService userService;

	@Autowired
	private VerifyCodeService verifyCodeService;

	@Autowired
	private CodeStoreRepository codeStoreRepository;

	@RequestMapping(path = "/register/code/{mobile}")
	public ResultBean<Void> sendRegisterVerifyCode(@PathVariable String mobile) {
		verifyCodeService.sendRegisterCode(mobile, REGISTER_CODE_CATEGORY, null);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/register/{code}")
	public ResultBean<Void> register(@RequestBody User merchant, @PathVariable String code) {
		Optional<CodeStore> codeStoreOp = codeStoreRepository.findByCategoryAndKey(REGISTER_CODE_CATEGORY, merchant.getUsername());
		AssertUtil.assertTrue(codeStoreOp.isPresent(), "验证码无效");
		AssertUtil.assertTrue(codeStoreOp.get().getValue().equals(code), "验证码无效");
		userService.register(merchant);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/register/exists/{mobile}")
	public ResultBean<Boolean> exists(@PathVariable String mobile) {
		Optional<Merchant> user = repository.findByUsername(mobile);
		return new ResultBean<>(user.isPresent());
	}

	@RequestMapping(path = "/forgetPassword/code/{mobile}")
	public ResultBean<Void> sendForgetPasswordCode(@PathVariable String mobile) {
		Optional<Merchant> merchant = repository.findByUsername(mobile);
		AssertUtil.assertTrue(merchant.isPresent(), "手机号不存在");
		verifyCodeService.sendForgetPasswordCode(mobile, FORGET_PASSWORD_CODE_CATEGORY, null);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/forgetPassword/{code}")
	public ResultBean<Void> resetPassword(@RequestBody User merchant, @PathVariable String code) {
		Optional<CodeStore> codeStoreOp = codeStoreRepository.findByCategoryAndKey(FORGET_PASSWORD_CODE_CATEGORY, merchant.getUsername());
		AssertUtil.assertTrue(codeStoreOp.isPresent(), "验证码无效");
		AssertUtil.assertTrue(codeStoreOp.get().getValue().equals(code), "验证码无效");
		userService.resetPassword(merchant);
		return new ResultBean<>();
	}

}
