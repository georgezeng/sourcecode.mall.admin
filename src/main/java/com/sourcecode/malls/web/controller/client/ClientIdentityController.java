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
import com.sourcecode.malls.domain.client.ClientIdentity;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.ClientIdentityBulkDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.client.ClientIdentityDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.VerificationStatus;
import com.sourcecode.malls.service.impl.client.ClientIdentityService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/client/identity")
public class ClientIdentityController extends BaseController {

	@Autowired
	private ClientIdentityService clientIdentityService;

	private String fileDir = "identity";

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<ClientIdentityDTO>> list(@RequestBody QueryInfo<ClientIdentityDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		queryInfo.getData().setMerchantId(user.getId());
		Page<ClientIdentity> result = clientIdentityService.findAll(queryInfo);
		PageResult<ClientIdentityDTO> dtoResult = new PageResult<>(
				result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()), result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<ClientIdentityDTO> load(@PathVariable Long id) {
		return new ResultBean<>(check(id).asDTO());
	}

	@RequestMapping(path = "/updateStatus")
	public ResultBean<Void> save(@RequestBody ClientIdentityBulkDTO dto) {
		for (Long id : dto.getIds().getIds()) {
			ClientIdentity data = check(id);
			if (dto.isPass()) {
				data.setStatus(VerificationStatus.Passed);
			} else {
				data.setReason(dto.getReason());
				data.setStatus(VerificationStatus.UnPassed);
			}
			clientIdentityService.save(data);
		}
		return new ResultBean<>();
	}

	private ClientIdentity check(Long id) {
		User user = getRelatedCurrentUser();
		Optional<ClientIdentity> dataOp = clientIdentityService.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(dataOp.get().getClient().getMerchant().getId().equals(user.getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
		return dataOp.get();
	}

	@RequestMapping(value = "/file/load/params/{id}", produces = { MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public Resource load(@RequestParam String filePath, @PathVariable Long id) {
		check(id);
		return load(id, filePath, fileDir, false);
	}

}
