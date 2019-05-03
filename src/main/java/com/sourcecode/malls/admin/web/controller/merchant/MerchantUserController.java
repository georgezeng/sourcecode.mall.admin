package com.sourcecode.malls.admin.web.controller.merchant;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.admin.domain.redis.CodeStore;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.repository.redis.impl.CodeStoreRepository;
import com.sourcecode.malls.admin.service.impl.VerifyCodeService;
import com.sourcecode.malls.admin.service.impl.merchant.MerchantUserService;
import com.sourcecode.malls.admin.util.AssertUtil;

@RestController
@RequestMapping(path = "/merchant")
public class MerchantUserController {
	private static final String REGISTER_CODE_CATEGORY = "merchant-register-code";
	private static final String FORGET_PASSWORD_CODE_CATEGORY = "merchant-forget-password-code";
	private static final String REGISTER_CODE_TIME_ATTR = "merchant-register-code-time";
	private static final String FORGET_PASSWORD_TIME_ATTR = "merchant-forget-password-code-time";

	@Autowired
	private MerchantUserService userService;

	@Autowired
	private VerifyCodeService verifyCodeService;

	@Autowired
	private CodeStoreRepository codeStoreRepository;

	@RequestMapping(path = "/register/code/{mobile}")
	public ResultBean<Void> sendRegisterVerifyCode(@PathVariable String mobile, HttpSession session) {
		verifyCodeService.sendRegisterCode(mobile, session, REGISTER_CODE_TIME_ATTR, REGISTER_CODE_CATEGORY);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/register/{code}")
	public ResultBean<Void> register(@RequestBody User merchant, @PathVariable String code) {
		Optional<CodeStore> codeStoreOp = codeStoreRepository.findByCategoryAndKey(REGISTER_CODE_CATEGORY, merchant.getUsername());
		AssertUtil.assertTrue(codeStoreOp.isPresent(), "验证码已过期");
		AssertUtil.assertTrue(codeStoreOp.get().getValue().equals(code), "验证码无效");
		userService.register(merchant);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/forgetPassword/code/{mobile}")
	public ResultBean<Void> sendForgetPasswordCode(@PathVariable String mobile, HttpSession session) {
		verifyCodeService.sendForgetPasswordCode(mobile, session, FORGET_PASSWORD_TIME_ATTR, FORGET_PASSWORD_CODE_CATEGORY);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/forgetPassword/{code}")
	public ResultBean<Void> resetPassword(@RequestBody User merchant, @PathVariable String code) {
		Optional<CodeStore> codeStoreOp = codeStoreRepository.findByCategoryAndKey(FORGET_PASSWORD_CODE_CATEGORY, merchant.getUsername());
		AssertUtil.assertTrue(codeStoreOp.isPresent(), "验证码已过期");
		AssertUtil.assertTrue(codeStoreOp.get().getValue().equals(code), "验证码无效");
		userService.resetPassword(merchant);
		return new ResultBean<>();
	}

}
