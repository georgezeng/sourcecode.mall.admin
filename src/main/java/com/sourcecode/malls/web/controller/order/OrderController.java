package com.sourcecode.malls.web.controller.order;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.order.Order;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.order.OrderDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.aftersale.AfterSaleApplicationRepository;
import com.sourcecode.malls.repository.jpa.impl.order.OrderRepository;
import com.sourcecode.malls.service.impl.order.OrderService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/order")
public class OrderController extends BaseController {

	@Autowired
	private OrderService orderService;

	@Autowired
	protected OrderRepository orderRepository;
	
	@Autowired
	protected AfterSaleApplicationRepository applicationRepository;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<OrderDTO>> list(@RequestBody QueryInfo<OrderDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		queryInfo.getData().setMerchantId(user.getId());
		Page<Order> result = orderService.getOrders(queryInfo);
		return new ResultBean<>(
				new PageResult<>(result.get().map(order -> order.asDTO(true, false)).collect(Collectors.toList()),
						result.getTotalElements()));
	}

	@RequestMapping(path = "/refund/list")
	public ResultBean<PageResult<OrderDTO>> refundList(@RequestBody QueryInfo<OrderDTO> queryInfo) {
		queryInfo.getData().setCancelForRefund(true);
		return list(queryInfo);
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<OrderDTO> load(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		Optional<Order> order = orderRepository.findById(id);
		AssertUtil.assertTrue(order.isPresent() && order.get().getMerchant().getId().equals(user.getId()),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		OrderDTO dto = order.get().asDTO(true, true);
		return new ResultBean<>(dto);
	}

	@RequestMapping(path = "/updateExpress")
	public ResultBean<Void> updateExpress(@RequestBody OrderDTO dto) {
		User user = getRelatedCurrentUser();
		orderService.updateExpress(user.getId(), dto);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/refund/approve/params/{id}")
	public ResultBean<Void> approveRefund(@PathVariable Long id) throws Exception {
		User user = getRelatedCurrentUser();
		orderService.approveRefund(user.getId(), id);
		return new ResultBean<>();
	}

}
