package com.sourcecode.malls.admin.web.controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.admin.context.UserContext;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.merchant.MerchantVerification;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.dto.merchant.MerchantVerificationDTO;
import com.sourcecode.malls.admin.enums.VerificationStatus;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantVerificationRepository;
import com.sourcecode.malls.admin.service.FileOnlineSystemService;
import com.sourcecode.malls.admin.util.AssertUtil;

@RestController
@RequestMapping(path = "/merchant/verification")
public class MerchantVerificationController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private MerchantVerificationRepository merchantVerificationRepository;

	@Autowired
	private FileOnlineSystemService fileService;

	@RequestMapping(path = "/load")
	public ResultBean<MerchantVerificationDTO> load() {
		Long currentUserId = UserContext.get().getId();
		Optional<MerchantVerification> oldDataOp = merchantVerificationRepository.findByMerchantId(currentUserId);
		return new ResultBean<>(oldDataOp.orElseGet(MerchantVerification::new).asDTO());
	}

	@RequestMapping(path = "/verify")
	public ResultBean<Void> verify(@RequestBody MerchantVerification verification) {
		Long currentUserId = UserContext.get().getId();
		Optional<MerchantVerification> oldDataOp = merchantVerificationRepository.findByMerchantId(currentUserId);
		AssertUtil.assertTrue(!oldDataOp.isPresent() || oldDataOp.isPresent() && oldDataOp.get().getStatus().equals(VerificationStatus.UnPassed),
				(oldDataOp.isPresent() && oldDataOp.get().getStatus().equals(VerificationStatus.Checking)) ? "认证正在审核中" : "认证已通过");
		if (oldDataOp.isPresent()) {
			BeanUtils.copyProperties(verification, oldDataOp.get(), "id", "createBy", "updateBy", "createTime", "updateTime", "merchant");
			verification = oldDataOp.get();
		} else {
			Optional<Merchant> merchant = merchantRepository.findById(currentUserId);
			AssertUtil.assertNotNull(merchant, "找不到商家记录");
			verification.setMerchant(merchant.get());
		}
		verification.setReason(null);
		verification.setStatus(VerificationStatus.Checking);
		merchantVerificationRepository.save(verification);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/update")
	public ResultBean<Void> update(@RequestBody MerchantVerification verification) {
		AssertUtil.assertNotNull(verification.getId(), "找不到认证信息");
		Long currentUserId = UserContext.get().getId();
		Optional<MerchantVerification> oldDataOp = merchantVerificationRepository.findByMerchantId(currentUserId);
		AssertUtil.assertNotNull(oldDataOp.isPresent(), "找不到认证信息");
		AssertUtil.assertTrue(verification.getId().equals(oldDataOp.get().getId()), "认证信息不匹配");
		MerchantVerification oldData = oldDataOp.get();
		oldData.setAddress(verification.getAddress());
		oldData.setContact(verification.getContact());
		oldData.setDescription(verification.getDescription());
		oldData.setPhone(verification.getPhone());
		merchantVerificationRepository.save(oldData);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/upload")
	public ResultBean<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
		String filePath = "merchant/" + UserContext.get().getId() + "/verificationLicense.png";
		fileService.upload(true, filePath, file.getInputStream());
		return new ResultBean<>(filePath);
	}
}
