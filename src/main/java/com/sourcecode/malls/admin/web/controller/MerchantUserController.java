package com.sourcecode.malls.admin.web.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.admin.domain.User;
import com.sourcecode.malls.admin.domain.redis.CodeStore;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.repository.redis.impl.CodeStoreRepository;
import com.sourcecode.malls.admin.service.impl.MerchantUserService;
import com.sourcecode.malls.admin.service.impl.SmsService;
import com.sourcecode.malls.admin.util.AssertUtil;
import com.sourcecode.malls.admin.util.CodeUtil;

@RestController
@RequestMapping(path = "/merchant")
public class MerchantUserController {
	private static final String REGISTER_CODE_KEY = "merchant-register-code";
	private static final String REGISTER_CODE_TIME_ATTR = "merchant-register-code-time";

	@Autowired
	private MerchantUserService userService;

	@Autowired
	private SmsService smsService;

	@Autowired
	private CodeStoreRepository codeStoreRepository;

	@RequestMapping(path = "/register/code/{mobile}")
	public ResultBean<Void> getRegisterVerifyCode(@PathVariable String mobile, HttpSession session) {
		Date sendTime = (Date) session.getAttribute(REGISTER_CODE_TIME_ATTR);
		if (sendTime != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(sendTime);
			c.add(Calendar.SECOND, 30);
			AssertUtil.assertTrue(new Date().after(c.getTime()), "操作太频繁，请稍后重试");
		}
		Optional<CodeStore> codeStoreOp = codeStoreRepository.findByCategoryAndKey(REGISTER_CODE_KEY, mobile);
		CodeStore codeStore = codeStoreOp.orElse(new CodeStore(REGISTER_CODE_KEY, mobile, CodeUtil.generateRandomNumbers(6)));
		if (codeStore.getId() == null) {
			codeStoreRepository.save(codeStore);
		}
		Map<String, Object> payload = new HashMap<>();
		payload.put("code", codeStore.getValue());
		smsService.send("SMS_162450479", mobile, payload);
		session.setAttribute(REGISTER_CODE_TIME_ATTR, new Date());
		return new ResultBean<>();
	}

	@RequestMapping(path = "/register/{code}")
	public ResultBean<Void> register(@RequestBody User merchant, @PathVariable String code) {
		Optional<CodeStore> codeStoreOp = codeStoreRepository.findByCategoryAndKey(REGISTER_CODE_KEY, merchant.getMobile());
		AssertUtil.assertTrue(codeStoreOp.isPresent(), "验证码已过期");
		AssertUtil.assertTrue(codeStoreOp.get().getValue().equals(code), "验证码无效");
		userService.register(merchant);
		return new ResultBean<>();
	}
}
