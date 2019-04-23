package com.sourcecode.malls.admin.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.admin.context.UserContext;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.merchant.MerchantVerification;
import com.sourcecode.malls.admin.domain.system.setting.User;
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
		MerchantVerification newData = new MerchantVerification();
		Optional<Merchant> merchant = merchantRepository.findById(currentUserId);
		newData.setMerchant(merchant.get());
		return new ResultBean<>(oldDataOp.orElse(newData).asDTO());
	}

	@RequestMapping(path = "/verify")
	public ResultBean<Void> verify(@RequestBody MerchantVerification verification) {
		Optional<MerchantVerification> oldDataOp = check(UserContext.get());
		if (oldDataOp.isPresent()) {
			BeanUtils.copyProperties(verification, oldDataOp.get(), "id", "createBy", "updateBy", "createTime", "updateTime", "merchant");
			verification = oldDataOp.get();
		} else {
			Optional<Merchant> merchant = merchantRepository.findById(UserContext.get().getId());
			AssertUtil.assertNotNull(merchant, "找不到商家记录");
			verification.setMerchant(merchant.get());
		}
		verification.setReason(null);
		verification.setStatus(VerificationStatus.Checking);

		String newPath = null;
		String tempPath = verification.getPhoto();
		if (tempPath != null && tempPath.startsWith("temp")) {
			newPath = "merchant/" + UserContext.get().getId() + "/verification.png";
			verification.setPhoto(newPath);
		}
		merchantVerificationRepository.save(verification);
		if (newPath != null) {
			byte[] buf = fileService.load(false, tempPath);
			fileService.upload(false, newPath, new ByteArrayInputStream(buf));
		}

		return new ResultBean<>();
	}

	private Optional<MerchantVerification> check(User user) {
		Optional<MerchantVerification> oldDataOp = merchantVerificationRepository.findByMerchantId(user.getId());
		if (oldDataOp.isPresent()) {
			AssertUtil.assertTrue(!oldDataOp.get().getStatus().equals(VerificationStatus.Checking), "正在审核中，不能重复提交");
			AssertUtil.assertTrue(!oldDataOp.get().getStatus().equals(VerificationStatus.Passed), "已经审核通过，不能再次提交");
		}
		return oldDataOp;
	}

	@RequestMapping(path = "/update")
	public ResultBean<Void> update(@RequestBody MerchantVerification verification) {
		AssertUtil.assertNotNull(verification.getId(), "找不到认证信息");
		Long currentUserId = UserContext.get().getId();
		Optional<MerchantVerification> oldDataOp = merchantVerificationRepository.findByMerchantId(currentUserId);
		AssertUtil.assertNotNull(oldDataOp.isPresent(), "找不到认证信息");
		AssertUtil.assertTrue(verification.getId().equals(oldDataOp.get().getId()), "找不到认证信息");
		AssertUtil.assertTrue(oldDataOp.get().getStatus().equals(VerificationStatus.Passed), "尚未通过认证，不能修改信息");
		MerchantVerification oldData = oldDataOp.get();
		oldData.setAddress(verification.getAddress());
		oldData.setContact(verification.getContact());
		oldData.setDescription(verification.getDescription());
		oldData.setPhone(verification.getPhone());
		merchantVerificationRepository.save(oldData);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/photo/upload")
	public ResultBean<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
		check(UserContext.get());
		String filePath = "temp/merchant/verification/" + UserContext.get().getId() + "/" + System.nanoTime() + ".png";
		fileService.upload(false, filePath, file.getInputStream());
		return new ResultBean<>(filePath);
	}

	@RequestMapping(value = "/photo/load")
	public Resource loadPhoto() {
		Optional<MerchantVerification> dataOp = merchantVerificationRepository.findByMerchantId(UserContext.get().getId());
		return new ByteArrayResource(fileService.load(false, dataOp.get().getPhoto()));
	}

	@RequestMapping(value = "/photo/preview")
	public Resource previewPhoto(@RequestParam String filePath) {
		AssertUtil.assertTrue(filePath.startsWith("temp/merchant/verification/" + UserContext.get().getId() + "/")
				|| filePath.equals("merchant/" + UserContext.get().getId() + "/verification.png"), "图片路径不合法");
		return new ByteArrayResource(fileService.load(false, filePath));
	}
}
