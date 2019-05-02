package com.sourcecode.malls.admin.web.controller.merchant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.admin.constants.ExceptionMessageConstant;
import com.sourcecode.malls.admin.context.UserContext;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.merchant.MerchantVerification;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.dto.merchant.MerchantVerificationDTO;
import com.sourcecode.malls.admin.enums.VerificationStatus;
import com.sourcecode.malls.admin.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.merchant.MerchantVerificationRepository;
import com.sourcecode.malls.admin.service.FileOnlineSystemService;
import com.sourcecode.malls.admin.util.AssertUtil;
import com.sourcecode.malls.admin.web.controller.base.BaseFileOperationController;

@RestController
@RequestMapping(path = "/merchant/verification")
public class MerchantVerificationController implements BaseFileOperationController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private MerchantVerificationRepository merchantVerificationRepository;

	@Autowired
	private FileOnlineSystemService fileService;

	private String fileDir = "merchant/verification";

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
			AssertUtil.assertNotNull(merchant, "商家信息不存在");
			verification.setMerchant(merchant.get());
		}
		verification.setReason(null);
		verification.setStatus(VerificationStatus.Checking);
		List<String> tmpPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		if (verification.getPhoto() != null && verification.getPhoto().startsWith("temp")) {
			String newPath = fileDir + "/" + UserContext.get().getId() + "/certificate.png";
			newPaths.add(newPath);
			tmpPaths.add(verification.getPhoto());
			verification.setPhoto(newPath);
		}
		merchantVerificationRepository.save(verification);
		transfer(fileService, false, tmpPaths, newPaths);
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
		Long currentUserId = UserContext.get().getId();
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
		return upload(fileService, file, fileDir, null, UserContext.get().getId(), false);
	}

	@RequestMapping(value = "/file/load")
	public Resource load(@RequestParam String filePath) {
		return load(fileService, UserContext.get().getId(), filePath, fileDir, false);
	}

}
