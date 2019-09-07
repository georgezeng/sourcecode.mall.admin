package com.sourcecode.malls.web.controller.client;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.domain.client.ClientLevelSetting;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.client.ClientLevelSettingDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.service.impl.client.ClientService;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/client/level/setting")
public class ClientLevelSettingController extends BaseController {

	@Autowired
	private ClientService clientService;

	private String fileDir = "merchant/client/level";

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<ClientLevelSettingDTO>> list() {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(clientService.findAllLevelSetting(user.getId()));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<ClientLevelSettingDTO> load(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(clientService.loadLevelSetting(user.getId(), id));
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody ClientLevelSettingDTO dto) {
		User user = getRelatedCurrentUser();
		ClientLevelSetting data = clientService.save(user.getId(), dto);
		if (dto.getImgPath().startsWith("temp")) {
			transfer(true, Arrays.asList(dto.getImgPath()), Arrays.asList(data.getImgPath()));
		}
		return new ResultBean<>();
	}

	@RequestMapping(path = "/clear/params/{id}")
	public ResultBean<Void> clear(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		clientService.clearLevelSetting(user.getId(), id);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/file/upload/params/{id}")
	public ResultBean<String> settingUpload(@RequestParam("file") MultipartFile file, @PathVariable Long id) throws IOException {
		return upload(file, fileDir, id, getRelatedCurrentUser().getId(), false);
	}

	@RequestMapping(value = "/file/load", produces = { MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public Resource load(@RequestParam String filePath) {
		return load(getRelatedCurrentUser().getId(), filePath, fileDir, false);
	}
}
