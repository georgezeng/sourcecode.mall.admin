package com.sourcecode.malls.admin.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aliyuncs.utils.StringUtils;
import com.sourcecode.malls.admin.context.UserContext;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.merchant.MerchantShopApplication;
import com.sourcecode.malls.admin.domain.merchant.MerchantShopApplicationInstruction;
import com.sourcecode.malls.admin.domain.merchant.MerchantVerification;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.dto.merchant.MerchantShopApplicationDTO;
import com.sourcecode.malls.admin.enums.VerificationStatus;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantShopApplicationRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantVerificationRepository;
import com.sourcecode.malls.admin.service.FileOnlineSystemService;
import com.sourcecode.malls.admin.util.AssertUtil;

@RestController
@RequestMapping(path = "/merchant/shop/application")
public class MerchantShopApplicationController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private MerchantVerificationRepository verificationRepository;

	@Autowired
	private MerchantShopApplicationRepository shopRepository;

	@Autowired
	private FileOnlineSystemService fileService;

	@RequestMapping(path = "/load")
	public ResultBean<MerchantShopApplicationDTO> load() {
		User user = UserContext.get();
		Optional<Merchant> merchant = merchantRepository.findById(user.getId());
		Optional<MerchantShopApplication> appOp = shopRepository.findByMerchant(merchant.get());
		MerchantShopApplicationDTO dto = appOp.orElseGet(MerchantShopApplication::new).asDTO();
		return new ResultBean<>(dto);
	}

	@RequestMapping(path = "/apply")
	public ResultBean<Void> apply(@RequestBody MerchantShopApplicationDTO dto) {
		User user = UserContext.get();
		check(user, false);
		Merchant merchant = merchantRepository.findById(user.getId()).get();
		MerchantShopApplication data = shopRepository.findByMerchant(merchant).orElseGet(MerchantShopApplication::new);
		if (data.getId() == null) {
			Optional<MerchantShopApplication> domainOp = shopRepository.findByDomain(dto.getDomain());
			AssertUtil.assertTrue(!domainOp.isPresent(), "域名已经被占用");
		}
		data.setMerchant(merchant);
		data.setDomain(dto.getDomain());
		data.setName(dto.getName());
		data.setAndroidType(dto.isAndroidType());
		data.setIosType(dto.isIosType());
		data.setDescription(dto.getDescription());
		if (data.getStatus() != null && data.getStatus().equals(VerificationStatus.UnPassed)) {
			data.setStatus(VerificationStatus.Checking);
		} else {
			data.setStatus(VerificationStatus.Checking); // for test
		}
		return update(dto, data);
	}

	private ResultBean<Void> update(MerchantShopApplicationDTO dto, MerchantShopApplication data) {
		List<String> tmpPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		if (dto.getAndroidSmallIcon() != null && dto.getAndroidSmallIcon().startsWith("temp")) {
			String newPath = "merchant/shop/application/" + UserContext.get().getId() + "/android_small_icon.png";
			String tmpPath = dto.getAndroidSmallIcon();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setAndroidSmallIcon(newPath);
		}
		if (dto.getAndroidBigIcon() != null && dto.getAndroidBigIcon().startsWith("temp")) {
			String newPath = "merchant/shop/application/" + UserContext.get().getId() + "/android_big_icon.png";
			String tmpPath = dto.getAndroidBigIcon();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setAndroidBigIcon(newPath);
		}
		if (dto.getIosSmallIcon() != null && dto.getIosSmallIcon().startsWith("temp")) {
			String newPath = "merchant/shop/application/" + UserContext.get().getId() + "/ios_small_icon.png";
			String tmpPath = dto.getIosSmallIcon();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setIosSmallIcon(newPath);
		}
		if (dto.getIosBigIcon() != null && dto.getIosBigIcon().startsWith("temp")) {
			String newPath = "merchant/shop/application/" + UserContext.get().getId() + "/ios_big_icon.png";
			String tmpPath = dto.getIosBigIcon();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setIosBigIcon(newPath);
		}
		if (dto.getLogo() != null && dto.getLogo().startsWith("temp")) {
			String newPath = "merchant/shop/application/" + UserContext.get().getId() + "/logo.png";
			String tmpPath = dto.getLogo();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setLogo(newPath);
		}
		if (dto.getLoginBgImg() != null && dto.getLoginBgImg().startsWith("temp")) {
			String newPath = "merchant/shop/application/" + UserContext.get().getId() + "/login_bg.png";
			String tmpPath = dto.getLoginBgImg();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setLoginBgImg(newPath);
		}
		List<MerchantShopApplicationInstruction> oldInstructions = data.getInstructions();
		data.setInstructions(null);
		int order = 0;
		for (String path : dto.getInstructions()) {
			MerchantShopApplicationInstruction instruction = null;
			if (oldInstructions != null && order < oldInstructions.size()) {
				instruction = oldInstructions.get(order);
			}
			if (instruction == null || path.startsWith("temp")) {
				instruction = new MerchantShopApplicationInstruction();
				String newPath = "merchant/shop/application/" + UserContext.get().getId() + "/instruction" + (order + 1) + ".png";
				newPaths.add(newPath);
				tmpPaths.add(path);
				instruction.setPath(newPath);
				instruction.setOrder(order + 1);
				instruction.setShopApplication(data);
			}
			data.addInstruction(instruction);
			order++;
		}
		data.setDeployed(false);
		shopRepository.save(data);
		for (int i = 0; i < newPaths.size(); i++) {
			String newPath = newPaths.get(i);
			String tmpPath = tmpPaths.get(i);
			byte[] buf = fileService.load(false, tmpPath);
			fileService.upload(false, newPath, new ByteArrayInputStream(buf));
		}
		return new ResultBean<>();
	}

	@RequestMapping(path = "/update")
	public ResultBean<Void> update(@RequestBody MerchantShopApplicationDTO dto) {
		User user = UserContext.get();
		check(user, true);
		Merchant merchant = merchantRepository.findById(user.getId()).get();
		Optional<MerchantShopApplication> dataOp = shopRepository.findByMerchant(merchant);
		AssertUtil.assertTrue(dataOp.isPresent(), "找不到申请记录");
		MerchantShopApplication data = dataOp.get();
		return update(dto, data);
	}

	private void check(User user, boolean isUpdate) {
		Optional<MerchantVerification> data = verificationRepository.findByMerchantId(user.getId());
		AssertUtil.assertTrue(data.isPresent() && data.get().getStatus().equals(VerificationStatus.Passed), "还没通过实名认证，请先进行实名认证");
		Optional<MerchantShopApplication> appOp = shopRepository.findByMerchant(merchantRepository.findById(user.getId()).get());
		if (appOp.isPresent()) {
			if (!isUpdate) {
				AssertUtil.assertTrue(!appOp.get().getStatus().equals(VerificationStatus.UnPay), "申请已经提交，不能重复提交");
				AssertUtil.assertTrue(!appOp.get().getStatus().equals(VerificationStatus.Checking), "正在审核中，不能重复提交");
				AssertUtil.assertTrue(!appOp.get().getStatus().equals(VerificationStatus.Passed), "已经审核通过，不能再次提交");
			} else {
				AssertUtil.assertTrue(!appOp.get().getStatus().equals(VerificationStatus.UnPay), "申请尚未支付，请前往支付");
				AssertUtil.assertTrue(!appOp.get().getStatus().equals(VerificationStatus.Checking), "正在审核中，不能提交");
				AssertUtil.assertTrue(!appOp.get().getStatus().equals(VerificationStatus.UnPassed), "审核不通过，请重新提交申请");
			}
		}
	}

	@RequestMapping(value = "/upload/params/{type}/{isUpdate}")
	public ResultBean<String> upload(@RequestParam("file") MultipartFile file, @PathVariable String type, @PathVariable Boolean isUpdate,
			@RequestParam(required = false) String extendDir) throws IOException {
		check(UserContext.get(), isUpdate);
		String dir = type + (StringUtils.isEmpty(extendDir) ? "" : extendDir);
		String filePath = "temp/shop/application/" + UserContext.get().getId() + "/" + dir + System.nanoTime() + ".png";
		fileService.upload(false, filePath, file.getInputStream());
		return new ResultBean<>(filePath);
	}

	@RequestMapping(value = "/img/load")
	public Resource loadImg(@RequestParam String filePath) {
		AssertUtil.assertTrue(filePath.startsWith("merchant/shop/application/" + UserContext.get().getId() + "/"), "图片路径不合法");
		return new ByteArrayResource(fileService.load(false, filePath));
	}

	@RequestMapping(value = "/img/preview")
	public Resource previewImg(@RequestParam String filePath) {
		AssertUtil.assertTrue(filePath.startsWith("temp/shop/application/" + UserContext.get().getId() + "/")
				|| filePath.startsWith("merchant/shop/application/" + UserContext.get().getId() + "/"), "图片路径不合法");
		return new ByteArrayResource(fileService.load(false, filePath));
	}

}
