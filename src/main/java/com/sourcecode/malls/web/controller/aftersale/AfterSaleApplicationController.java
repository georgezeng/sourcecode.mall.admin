package com.sourcecode.malls.web.controller.aftersale;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.domain.aftersale.AfterSaleApplication;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.aftersale.AfterSaleApplicationDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.AfterSaleStatus;
import com.sourcecode.malls.exception.BusinessException;
import com.sourcecode.malls.repository.jpa.impl.aftersale.AfterSaleApplicationRepository;
import com.sourcecode.malls.service.impl.aftersale.AfterSaleService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/afterSale/application")
public class AfterSaleApplicationController extends BaseController {

	@Autowired
	private AfterSaleService service;

	@Autowired
	private AfterSaleApplicationRepository repository;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<AfterSaleApplicationDTO>> list(
			@RequestBody QueryInfo<AfterSaleApplicationDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getList(user.getId(), queryInfo));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<AfterSaleApplicationDTO> load(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.load(user.getId(), id).asDTO());
	}

	@RequestMapping(path = "/audit")
	public ResultBean<Void> audit(@RequestBody AfterSaleApplicationDTO dto) {
		User user = getRelatedCurrentUser();
		AfterSaleApplication data = service.load(user.getId(), dto.getId());
		AssertUtil.assertTrue(AfterSaleStatus.Processing.equals(data.getStatus()), "状态有误，不能审核该申请记录");
		AfterSaleStatus status = null;
		if (dto.isAgree()) {
			switch (data.getType()) {
			case Change:
			case SalesReturn:
				status = AfterSaleStatus.WaitForReturn;
				break;
			case RefundOnly:
				status = AfterSaleStatus.WaitForRefund;
				break;
			default:
				throw new BusinessException("暂不支持该类型的操作");
			}
		} else {
			AssertUtil.assertNotEmpty(dto.getRejectReason(), "拒绝原因不能为空");
			status = AfterSaleStatus.Rejected;
			data.setRejectReason(dto.getRejectReason());
		}
		data.setStatus(status);
		data.setProcessedTime(new Date());
		repository.save(data);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/refund")
	public ResultBean<Void> refund(@RequestBody AfterSaleApplicationDTO dto) throws Exception {
		User user = getRelatedCurrentUser();
		service.refund(user.getId(), dto);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/receive/params/{id}")
	public ResultBean<Void> receive(@PathVariable Long id) throws Exception {
		User user = getRelatedCurrentUser();
		service.receive(user.getId(), id);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/sent")
	public ResultBean<Void> sent(@RequestBody AfterSaleApplicationDTO dto) throws Exception {
		User user = getRelatedCurrentUser();
		service.sent(user.getId(), dto);
		return new ResultBean<>();
	}

}
