package com.sourcecode.malls.web.controller.setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.setting.DeveloperSettingDTO;
import com.sourcecode.malls.service.impl.MerchantSettingService;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/setting/alipay")
public class AlipaySettingController extends BaseController {

	@Autowired
	private MerchantSettingService settingService;

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody DeveloperSettingDTO setting) {
		settingService.saveAlipay(setting, getRelatedCurrentUser().getId());
		return new ResultBean<>();
	}

	@RequestMapping(path = "/load")
	public ResultBean<DeveloperSettingDTO> load() {
		return new ResultBean<>(settingService.loadAlipay(getRelatedCurrentUser().getId()).orElseGet(DeveloperSettingDTO::new));
	}

}
