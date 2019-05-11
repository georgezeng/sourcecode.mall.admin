package com.sourcecode.malls.web.controller.merchant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.domain.merchant.MerchantVerification;
import com.sourcecode.malls.domain.system.setting.User;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.merchant.MerchantVerificationDTO;
import com.sourcecode.malls.enums.VerificationStatus;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantVerificationRepository;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/merchant/verification")
public class MerchantVerificationController extends BaseController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private MerchantVerificationRepository merchantVerificationRepository;

	private String fileDir = "merchant/verification";

	@RequestMapping(path = "/load")
	public ResultBean<MerchantVerificationDTO> load() {
		Long currentUserId = getRelatedCurrentUser().getId();
		Optional<MerchantVerification> oldDataOp = merchantVerificationRepository.findByMerchantId(currentUserId);
		MerchantVerification newData = new MerchantVerification();
		Optional<Merchant> merchant = merchantRepository.findById(currentUserId);
		newData.setMerchant(merchant.get());
		return new ResultBean<>(oldDataOp.orElse(newData).asDTO());
	}

	@RequestMapping(path = "/verify")
	public ResultBean<Void> verify(@RequestBody MerchantVerification verification) {
		User user = getRelatedCurrentUser();
		Optional<MerchantVerification> oldDataOp = check(user);
		if (oldDataOp.isPresent()) {
			BeanUtils.copyProperties(verification, oldDataOp.get(), "id", "createBy", "updateBy", "createTime", "updateTime", "merchant");
			verification = oldDataOp.get();
		} else {
			Optional<Merchant> merchant = merchantRepository.findById(user.getId());
			AssertUtil.assertNotNull(merchant, "商家信息不存在");
			verification.setMerchant(merchant.get());
		}
		verification.setReason(null);
		verification.setStatus(VerificationStatus.Checking);
		List<String> tmpPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		if (verification.getPhoto() != null && verification.getPhoto().startsWith("temp")) {
			String newPath = fileDir + "/" + user.getId() + "/certificate.png";
			newPaths.add(newPath);
			tmpPaths.add(verification.getPhoto());
			verification.setPhoto(newPath);
		}
		merchantVerificationRepository.save(verification);
		transfer(false, tmpPaths, newPaths);
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
		AssertUtil.assertNotNull(verification.getId(), ExceptionMessageConstant.NO_SUCH_RECORD);
		Long currentUserId = getRelatedCurrentUser().getId();
		Optional<MerchantVerification> oldDataOp = merchantVerificationRepository.findByMerchantId(currentUserId);
		AssertUtil.assertNotNull(oldDataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(verification.getId().equals(oldDataOp.get().getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(oldDataOp.get().getStatus().equals(VerificationStatus.Passed), "尚未通过认证，不能修改信息");
		MerchantVerification oldData = oldDataOp.get();
		oldData.setAddress(verification.getAddress());
		oldData.setContact(verification.getContact());
		oldData.setDescription(verification.getDescription());
		oldData.setPhone(verification.getPhone());
		merchantVerificationRepository.save(oldData);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/file/upload")
	public ResultBean<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
		return upload(file, fileDir, null, getRelatedCurrentUser().getId(), false);
	}

	@RequestMapping(value = "/file/load", produces = { MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public Resource load(@RequestParam String filePath) {
		return load(getRelatedCurrentUser().getId(), filePath, fileDir, false);
	}

}
