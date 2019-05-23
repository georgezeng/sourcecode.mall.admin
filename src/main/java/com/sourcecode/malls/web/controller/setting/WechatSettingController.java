package com.sourcecode.malls.web.controller.setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.WechatInfo;
import com.sourcecode.malls.context.UserContext;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.setting.DeveloperSettingDTO;
import com.sourcecode.malls.service.impl.MerchantSettingService;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/setting/wechat")
public class WechatSettingController extends BaseController {

	@Autowired
	private MerchantSettingService settingService;

	@RequestMapping(path = "/gzh/save")
	public ResultBean<Void> save(@RequestBody DeveloperSettingDTO setting) {
		settingService.saveWechatGzh(setting, UserContext.get().getId());
		return new ResultBean<>();
	}

	@RequestMapping(path = "/load")
	public ResultBean<WechatInfo> load() {
		WechatInfo info = new WechatInfo();
		info.setGzhInfo(settingService.loadWechatGzh(UserContext.get().getId()).orElseGet(DeveloperSettingDTO::new));
		return new ResultBean<>(info);
	}

}
