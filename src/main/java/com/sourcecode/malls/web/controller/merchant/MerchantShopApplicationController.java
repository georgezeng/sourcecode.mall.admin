package com.sourcecode.malls.web.controller.merchant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
import com.sourcecode.malls.domain.merchant.MerchantShopApplication;
import com.sourcecode.malls.domain.merchant.MerchantShopApplicationInstruction;
import com.sourcecode.malls.domain.merchant.MerchantVerification;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.merchant.MerchantShopApplicationDTO;
import com.sourcecode.malls.enums.VerificationStatus;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantShopApplicationRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantVerificationRepository;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/merchant/shop/application")
public class MerchantShopApplicationController extends BaseController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private MerchantVerificationRepository verificationRepository;

	@Autowired
	private MerchantShopApplicationRepository shopRepository;

	private String fileDir = "merchant/shop";

	@RequestMapping(path = "/load")
	public ResultBean<MerchantShopApplicationDTO> load() {
		User user = getRelatedCurrentUser();
		Optional<MerchantVerification> verificationOp = verificationRepository.findByMerchantId(user.getId());
		if (verificationOp.isPresent() && VerificationStatus.Passed.equals(verificationOp.get().getStatus())) {
			Optional<MerchantShopApplication> appOp = shopRepository.findByMerchantId(user.getId());
			MerchantShopApplicationDTO dto = appOp.orElseGet(MerchantShopApplication::new).asDTO();
			if (appOp.isPresent()) {
				dto.setNoPermit(!VerificationStatus.Passed.equals(appOp.get().getStatus()));
			}
			return new ResultBean<>(dto);
		} else {
			MerchantShopApplicationDTO dto = new MerchantShopApplicationDTO();
			dto.setNoPermit(true);
			return new ResultBean<>(dto);
		}
	}

	@RequestMapping(path = "/apply")
	public ResultBean<Void> apply(@RequestBody MerchantShopApplicationDTO dto) {
		User user = getRelatedCurrentUser();
		check(user, false);
		Merchant merchant = merchantRepository.findById(user.getId()).get();
		MerchantShopApplication data = shopRepository.findByMerchantId(merchant.getId()).orElseGet(MerchantShopApplication::new);
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
		User user = getRelatedCurrentUser();
		if (dto.getAndroidSmallIcon() != null && dto.getAndroidSmallIcon().startsWith("temp")) {
			String newPath = fileDir + "/" + user.getId() + "/android_small_icon.png";
			String tmpPath = dto.getAndroidSmallIcon();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setAndroidSmallIcon(newPath);
		}
		if (dto.getAndroidBigIcon() != null && dto.getAndroidBigIcon().startsWith("temp")) {
			String newPath = fileDir + "/" + user.getId() + "/android_big_icon.png";
			String tmpPath = dto.getAndroidBigIcon();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setAndroidBigIcon(newPath);
		}
		if (dto.getIosSmallIcon() != null && dto.getIosSmallIcon().startsWith("temp")) {
			String newPath = fileDir + "/" + user.getId() + "/ios_small_icon.png";
			String tmpPath = dto.getIosSmallIcon();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setIosSmallIcon(newPath);
		}
		if (dto.getIosBigIcon() != null && dto.getIosBigIcon().startsWith("temp")) {
			String newPath = fileDir + "/" + user.getId() + "/ios_big_icon.png";
			String tmpPath = dto.getIosBigIcon();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setIosBigIcon(newPath);
		}
		if (dto.getLogo() != null && dto.getLogo().startsWith("temp")) {
			String newPath = fileDir + "/" + user.getId() + "/logo.png";
			String tmpPath = dto.getLogo();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setLogo(newPath);
		}
		if (dto.getLoginBgImg() != null && dto.getLoginBgImg().startsWith("temp")) {
			String newPath = fileDir + "/" + user.getId() + "/login_bg.png";
			String tmpPath = dto.getLoginBgImg();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setLoginBgImg(newPath);
		}
		List<MerchantShopApplicationInstruction> instructions = data.getInstructions();
		if (instructions == null) {
			instructions = new ArrayList<>();
			data.setInstructions(instructions);
		}
		int order = 0;
		for (Iterator<MerchantShopApplicationInstruction> it = instructions.iterator(); it.hasNext();) {
			MerchantShopApplicationInstruction instruction = it.next();
			String path = null;
			if (order < dto.getInstructions().size()) {
				path = dto.getInstructions().get(order);
			}
			if (path == null) {
				it.remove();
			} else if (path.startsWith("temp")) {
				String newPath = fileDir + "/" + user.getId() + "/instruction_" + (order + 1) + ".png";
				newPaths.add(newPath);
				tmpPaths.add(path);
				instruction.setPath(newPath);
				order++;
			} else if (!path.equals(instruction.getPath())) {
				instruction.setPath(path);
				order++;
			} else {
				order++;
			}
		}
		if (order < dto.getInstructions().size()) {
			for (int i = order; i < dto.getInstructions().size(); i++) {
				MerchantShopApplicationInstruction instruction = new MerchantShopApplicationInstruction();
				instruction.setOrder(i + 1);
				instruction.setShopApplication(data);
				String path = dto.getInstructions().get(i);
				String newPath = fileDir + "/" + user.getId() + "/instruction_" + (order + 1) + ".png";
				newPaths.add(newPath);
				tmpPaths.add(path);
				instruction.setPath(newPath);
				instructions.add(instruction);
			}
		}
		data.setReason(null);
		data.setDeployed(false);
		shopRepository.save(data);
		transfer(true, tmpPaths, newPaths);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/update")
	public ResultBean<Void> update(@RequestBody MerchantShopApplicationDTO dto) {
		User user = getRelatedCurrentUser();
		check(user, true);
		Optional<MerchantShopApplication> dataOp = shopRepository.findByMerchantId(user.getId());
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		MerchantShopApplication data = dataOp.get();
		return update(dto, data);
	}

	private void check(User user, boolean isUpdate) {
		Optional<MerchantVerification> data = verificationRepository.findByMerchantId(user.getId());
		AssertUtil.assertTrue(data.isPresent() && data.get().getStatus().equals(VerificationStatus.Passed), "还没通过实名认证，请先进行实名认证");
		Optional<MerchantShopApplication> appOp = shopRepository.findByMerchantId(user.getId());
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

	@RequestMapping(value = "/file/upload")
	public ResultBean<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
		User user = getRelatedCurrentUser();
		return upload(file, fileDir, null, user.getId(), false);
	}

	@RequestMapping(value = "/file/load", produces = { MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public Resource load(@RequestParam String filePath) {
		User user = getRelatedCurrentUser();
		return load(user.getId(), filePath, fileDir, true);
	}

}
