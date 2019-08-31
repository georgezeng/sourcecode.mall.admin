package com.sourcecode.malls.web.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.client.ClientActivityEventDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.service.impl.client.ClientService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/client/activityEvent")
public class ClientActivityEventController extends BaseController {

	@Autowired
	private ClientService clientService;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<ClientActivityEventDTO>> list(@RequestBody QueryInfo<SimpleQueryDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(clientService.findAllActivityEvents(user.getId(), queryInfo));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<ClientActivityEventDTO> load(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(clientService.loadActivityEvent(user.getId(), id));
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody ClientActivityEventDTO dto) {
		User user = getRelatedCurrentUser();
		clientService.save(user.getId(), dto);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/trigger/params/{id}/{enable}")
	public ResultBean<Void> trigger(@PathVariable("id") Long id, @PathVariable("enable") boolean enable) {
		User user = getRelatedCurrentUser();
		clientService.triggerPauseActivityEvent(user.getId(), id, enable);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/delete")
	public ResultBean<Void> clear(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()),
				ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		User user = getRelatedCurrentUser();
		clientService.deleteActivityEvents(user.getId(), keys.getIds());
		return new ResultBean<>();
	}

}
