package com.sourcecode.malls.web.controller.coupon;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.coupon.cash.CashCouponSetting;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.coupon.cash.CashClientCouponDTO;
import com.sourcecode.malls.dto.coupon.cash.CashCouponHxDTO;
import com.sourcecode.malls.dto.coupon.cash.CashCouponSettingDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.coupon.CashCouponSettingRepository;
import com.sourcecode.malls.service.impl.coupon.CashCouponService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/coupon/cash")
public class CashCouponController extends BaseController {

	@Autowired
	private CashCouponService service;

	@Autowired
	protected CashCouponSettingRepository settingRepository;

	private String fileDir = "coupon";

	@RequestMapping(path = "/setting/list")
	public ResultBean<PageResult<CashCouponSettingDTO>> settingList(@RequestBody QueryInfo<SimpleQueryDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getSettingList(user.getId(), queryInfo));
	}

	@RequestMapping(path = "/setting/load/params/{id}")
	public ResultBean<CashCouponSettingDTO> getSetting(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.get(user.getId(), id));
	}

	@RequestMapping(path = "/client/list")
	public ResultBean<PageResult<CashClientCouponDTO>> clientList(
			@RequestBody QueryInfo<CashClientCouponDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getClientList(user.getId(), queryInfo));
	}

	@RequestMapping(path = "/setting/save/baseInfo")
	public ResultBean<Long> saveBaseInfo(@RequestBody CashCouponSettingDTO dto) {
		Long userId = getRelatedCurrentUser().getId();
		CashCouponSetting data = service.saveBaseInfo(userId, dto);
		if (data.getImgPath().startsWith("temp")) {
			String newPath = fileDir + "/" + userId + "/" + data.getId() + "/" + System.nanoTime() + ".png";
			transfer(true, Arrays.asList(data.getImgPath()), Arrays.asList(newPath));
			data.setImgPath(newPath);
		}
		settingRepository.save(data);
		return new ResultBean<>(data.getId());
	}

	@RequestMapping(path = "/setting/save/condition/zs")
	public ResultBean<Void> saveZsCondition(@RequestBody CashCouponSettingDTO dto) {
		service.saveZsCondition(getRelatedCurrentUser().getId(), dto);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/setting/save/condition/hx")
	public ResultBean<Void> saveHxCondition(@RequestBody CashCouponHxDTO dto) {
		service.saveHxCondition(getRelatedCurrentUser().getId(), dto);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/setting/updateStatus/params/{status}")
	public ResultBean<Void> updateStatus(@RequestBody KeyDTO<Long> keys, @PathVariable Boolean status) {
		for (Long id : keys.getIds()) {
			service.updateStatus(getRelatedCurrentUser().getId(), id, status);
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/setting/file/upload/params/{id}")
	public ResultBean<String> settingUpload(@RequestParam("file") MultipartFile file, @PathVariable Long id)
			throws IOException {
		return upload(file, fileDir, id, getRelatedCurrentUser().getId(), false);
	}

	@RequestMapping(value = "/setting/file/load", produces = { MediaType.IMAGE_PNG_VALUE,
			MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public Resource load(@RequestParam String filePath) {
		return load(getRelatedCurrentUser().getId(), filePath, fileDir, false);
	}

	@RequestMapping(value = "/setting/delete")
	public ResultBean<Void> deleteSetting(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()),
				ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		User user = getRelatedCurrentUser();
		for (Long id : keys.getIds()) {
			Optional<CashCouponSetting> dataOp = settingRepository.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				settingRepository.delete(dataOp.get());
			}
		}
		return new ResultBean<>();
	}
}
