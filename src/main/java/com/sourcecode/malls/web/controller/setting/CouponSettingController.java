package com.sourcecode.malls.web.controller.setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.coupon.CouponMerchantSettingDTO;
import com.sourcecode.malls.service.impl.MerchantSettingService;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/setting/coupon")
public class CouponSettingController extends BaseController {

	@Autowired
	private MerchantSettingService settingService;

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody CouponMerchantSettingDTO setting) {
		settingService.saveCouponSetting(getRelatedCurrentUser().getId(), setting);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/load")
	public ResultBean<CouponMerchantSettingDTO> load() {
		return new ResultBean<>(settingService.loadCouponSetting(getRelatedCurrentUser().getId()));
	}

}
