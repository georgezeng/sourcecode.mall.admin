package com.sourcecode.malls.admin.web.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.util.StringUtils;
import com.sourcecode.malls.admin.domain.redis.CodeStore;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.repository.redis.impl.CodeStoreRepository;
import com.sourcecode.malls.admin.service.impl.MerchantUserService;
import com.sourcecode.malls.admin.service.impl.SmsService;
import com.sourcecode.malls.admin.util.CodeUtil;

@RestController
@RequestMapping(path = "/merchant")
public class MerchantUserController {
	private static final String REGISTER_CODE_KEY = "merchant-register-code";

	@Autowired
	private MerchantUserService userService;

	@Autowired
	private SmsService smsService;

	@Autowired
	private CodeStoreRepository codeStoreRepository;

	@RequestMapping(path = "/register/code/{mobile}")
	public ResultBean<Void> getRegisterVerifyCode(@PathVariable String mobile, HttpSession session) {
		Optional<CodeStore> codeStoreOp = codeStoreRepository.findByCategoryAndKey(REGISTER_CODE_KEY, session.getId());
		CodeStore codeStore = codeStoreOp.orElse(new CodeStore(REGISTER_CODE_KEY, session.getId(), CodeUtil.generateRandomNumbers(6)));
		if (codeStore.getId() == null) {
			codeStoreRepository.save(codeStore);
		}
		Map<String, Object> payload = new HashMap<>();
		payload.put("code", codeStore.getValue());
		smsService.send("", mobile, payload);
		return new ResultBean<>();
	}
}
