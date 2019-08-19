package com.sourcecode.malls.web.controller.coupon;

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
import com.sourcecode.malls.dto.coupon.cash.CashCouponOrderLimitedSettingDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.service.impl.coupon.CashCouponService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/coupon/cash/orderLimited/setting")
public class CashCouponOrderLimitedSettingController extends BaseController {

	@Autowired
	private CashCouponService service;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<CashCouponOrderLimitedSettingDTO>> getList(@RequestBody QueryInfo<Void> queryInfo) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getOrderLimitedSettingList(user.getId(), queryInfo));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<CashCouponOrderLimitedSettingDTO> get(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getOrderLimitedSetting(id, user.getId()));
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody CashCouponOrderLimitedSettingDTO dto) {
		Long userId = getRelatedCurrentUser().getId();
		service.saveOrderLimitedSetting(userId, dto);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()),
				ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		User user = getRelatedCurrentUser();
		service.deleteOrderLimitedSetting(user.getId(), keys.getIds());
		return new ResultBean<>();
	}
}
