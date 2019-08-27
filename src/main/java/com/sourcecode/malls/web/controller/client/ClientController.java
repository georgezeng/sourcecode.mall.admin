package com.sourcecode.malls.web.controller.client;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.client.Client;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.client.ClientDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.service.impl.client.ClientService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/client/user")
public class ClientController extends BaseController {

	@Autowired
	private ClientService clientService;

	private String fileDir = "client";

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<ClientDTO>> list(@RequestBody QueryInfo<ClientDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		queryInfo.getData().setMerchantId(user.getId());
		Page<Client> result = clientService.findAll(queryInfo);
		PageResult<ClientDTO> dtoResult = new PageResult<>(result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()),
				result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}
	
	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<ClientDTO> load(@PathVariable Long id) {
		return new ResultBean<>(check(id).asDTO());
	}

	@RequestMapping(path = "/updateStatus/params/{enabled}")
	public ResultBean<Void> save(@PathVariable Boolean enabled, @RequestBody KeyDTO<Long> dto) {
		for (Long id : dto.getIds()) {
			Client data = check(id);
			data.setEnabled(enabled);
			clientService.save(data);
		}
		return new ResultBean<>();
	}

	private Client check(Long id) {
		User user = getRelatedCurrentUser();
		Optional<Client> dataOp = clientService.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
		return dataOp.get();
	}

	@RequestMapping(value = "/file/load/params/{id}", produces = { MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public Resource load(@RequestParam String filePath, @PathVariable Long id) {
		Client data = check(id);
		return load(data.getId(), filePath, fileDir, false);
	}

}
