package com.sourcecode.malls;

import com.sourcecode.malls.dto.setting.DeveloperSettingDTO;

public class WechatInfo {
	private DeveloperSettingDTO gzhInfo;
	private DeveloperSettingDTO shhInfo;

	public DeveloperSettingDTO getGzhInfo() {
		return gzhInfo;
	}

	public void setGzhInfo(DeveloperSettingDTO gzhInfo) {
		this.gzhInfo = gzhInfo;
	}

	public DeveloperSettingDTO getShhInfo() {
		return shhInfo;
	}

	public void setShhInfo(DeveloperSettingDTO shhInfo) {
		this.shhInfo = shhInfo;
	}
}
