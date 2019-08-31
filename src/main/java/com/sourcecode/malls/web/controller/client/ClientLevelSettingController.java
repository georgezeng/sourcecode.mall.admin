package com.sourcecode.malls.web.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
		clientService.save(user.getId(), dto);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/clear/params/{id}")
	public ResultBean<Void> clear(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		clientService.clearLevelSetting(user.getId(), id);
		return new ResultBean<>();
	}

}
