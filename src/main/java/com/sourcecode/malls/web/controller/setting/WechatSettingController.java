package com.sourcecode.malls.web.controller.setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.WechatInfo;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.setting.DeveloperSettingDTO;
import com.sourcecode.malls.service.impl.MerchantSettingService;
import com.sourcecode.malls.service.impl.WechatService;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/setting/wechat")
public class WechatSettingController extends BaseController {

	@Autowired
	private MerchantSettingService settingService;
	
	@Autowired
	private WechatService wechatService;

	@RequestMapping(path = "/gzh/save")
	public ResultBean<Void> save(@RequestBody DeveloperSettingDTO setting) {
		settingService.saveWechatGzh(setting, getRelatedCurrentUser().getId());
		wechatService.clearWePayConfig(getRelatedCurrentUser().getId());
		return new ResultBean<>();
	}

	@RequestMapping(path = "/pay/cert/upload")
	public ResultBean<Void> uploadPayCert(@RequestParam("file") MultipartFile file) throws Exception {
		settingService.uploadWepayCert(file, getRelatedCurrentUser().getId());
		wechatService.clearWePayConfig(getRelatedCurrentUser().getId());
		return new ResultBean<>();
	}

	@RequestMapping(path = "/gzh/load")
	public ResultBean<WechatInfo> load() {
		WechatInfo info = new WechatInfo();
		info.setGzhInfo(
				settingService.loadWechatGzh(getRelatedCurrentUser().getId()).orElseGet(DeveloperSettingDTO::new));
		return new ResultBean<>(info);
	}

}
