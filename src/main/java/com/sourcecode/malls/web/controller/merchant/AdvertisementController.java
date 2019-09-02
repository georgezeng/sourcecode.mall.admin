package com.sourcecode.malls.web.controller.merchant;

import java.io.IOException;
import java.util.Arrays;

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
import com.sourcecode.malls.domain.merchant.AdvertisementSetting;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.merchant.AdvertisementSettingDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.merchant.AdvertisementSettingRepository;
import com.sourcecode.malls.service.impl.merchant.AdvertisementService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/advertisement")
public class AdvertisementController extends BaseController {

	@Autowired
	private AdvertisementService service;

	@Autowired
	private AdvertisementSettingRepository repository;

	private String fileDir = "advertisement";

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<AdvertisementSettingDTO>> list(@RequestBody QueryInfo<SimpleQueryDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getList(user.getId(), queryInfo));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<AdvertisementSettingDTO> getSetting(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.get(user.getId(), id));
	}

	@RequestMapping(path = "/save")
	public ResultBean<Long> saveBaseInfo(@RequestBody AdvertisementSettingDTO dto) {
		Long userId = getRelatedCurrentUser().getId();
		AdvertisementSetting data = service.save(userId, dto);
		if (data.getPath().startsWith("temp")) {
			String newPath = fileDir + "/" + userId + "/" + data.getId() + "/" + System.nanoTime() + ".png";
			transfer(true, Arrays.asList(data.getPath()), Arrays.asList(newPath));
			data.setPath(newPath);
		}
		repository.save(data);
		return new ResultBean<>(data.getId());
	}

	@RequestMapping(value = "/file/upload/params/{id}")
	public ResultBean<String> settingUpload(@RequestParam("file") MultipartFile file, @PathVariable Long id)
			throws IOException {
		return upload(file, fileDir, id, getRelatedCurrentUser().getId(), false);
	}

	@RequestMapping(value = "/file/load", produces = { MediaType.IMAGE_PNG_VALUE,
			MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public Resource load(@RequestParam String filePath) {
		return load(getRelatedCurrentUser().getId(), filePath, fileDir, false);
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> deleteSetting(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()),
				ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		User user = getRelatedCurrentUser();
		service.delete(user.getId(), keys.getIds());
		return new ResultBean<>();
	}
}
