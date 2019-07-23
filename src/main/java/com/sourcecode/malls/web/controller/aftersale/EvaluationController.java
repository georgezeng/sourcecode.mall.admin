package com.sourcecode.malls.web.controller.aftersale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.goods.GoodsItemEvaluationDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.service.impl.aftersale.EvaluationService;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/evaluation")
public class EvaluationController extends BaseController {
	@Autowired
	private EvaluationService service;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<GoodsItemEvaluationDTO>> list(
			@RequestBody QueryInfo<GoodsItemEvaluationDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getList(user.getId(), queryInfo));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsItemEvaluationDTO> load(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.load(user.getId(), id).asDTO());
	}

	@RequestMapping(path = "/reply")
	public ResultBean<Void> reply(@RequestBody GoodsItemEvaluationDTO dto) {
		User user = getRelatedCurrentUser();
		service.reply(user.getId(), dto);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/open")
	public ResultBean<Void> open(@RequestBody GoodsItemEvaluationDTO dto) {
		User user = getRelatedCurrentUser();
		service.open(user.getId(), dto);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/audit")
	public ResultBean<Void> audit(@RequestBody GoodsItemEvaluationDTO dto) {
		User user = getRelatedCurrentUser();
		service.audit(user.getId(), dto);
		return new ResultBean<>();
	}
}
