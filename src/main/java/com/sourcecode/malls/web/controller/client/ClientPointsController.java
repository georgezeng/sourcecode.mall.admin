package com.sourcecode.malls.web.controller.client;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.domain.client.ClientPoints;
import com.sourcecode.malls.domain.client.ClientPointsJournal;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.client.ClientPointsDTO;
import com.sourcecode.malls.dto.client.ClientPointsJournalDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.service.impl.client.ClientService;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/client/points")
public class ClientPointsController extends BaseController {

	@Autowired
	private ClientService clientService;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<ClientPointsDTO>> list(@RequestBody QueryInfo<SimpleQueryDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		Page<ClientPoints> result = clientService.findAllPoints(user.getId(), queryInfo);
		return new ResultBean<>(new PageResult<>(result.get().map(data -> data.asDTO()).collect(Collectors.toList()),
				result.getTotalElements()));
	}

	@RequestMapping(path = "/journals/list")
	public ResultBean<PageResult<ClientPointsJournalDTO>> journalList(
			@RequestBody QueryInfo<SimpleQueryDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		Page<ClientPointsJournal> result = clientService.findAllPointsJournal(user.getId(), queryInfo);
		return new ResultBean<>(new PageResult<>(result.get().map(data -> data.asDTO()).collect(Collectors.toList()),
				result.getTotalElements()));
	}

	@RequestMapping(path = "/journals/create")
	public ResultBean<Void> journalList(@RequestBody ClientPointsJournalDTO dto) {
		User user = getRelatedCurrentUser();
		clientService.createPointsJournal(user.getId(), dto);
		return new ResultBean<>();
	}

}
