package com.sourcecode.malls.service.impl.order;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.github.wxpay.sdk.WePayConfig;
import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.order.Express;
import com.sourcecode.malls.domain.order.Order;
import com.sourcecode.malls.domain.order.SubOrder;
import com.sourcecode.malls.dto.order.ExpressDTO;
import com.sourcecode.malls.dto.order.OrderDTO;
import com.sourcecode.malls.dto.order.SubOrderDTO;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.ExpressType;
import com.sourcecode.malls.enums.OrderStatus;
import com.sourcecode.malls.exception.BusinessException;
import com.sourcecode.malls.repository.jpa.impl.order.ExpressRepository;
import com.sourcecode.malls.repository.jpa.impl.order.OrderRepository;
import com.sourcecode.malls.repository.jpa.impl.order.SubOrderRepository;
import com.sourcecode.malls.service.base.BaseService;
import com.sourcecode.malls.service.impl.AlipayService;
import com.sourcecode.malls.service.impl.CacheClearer;
import com.sourcecode.malls.service.impl.WechatService;
import com.sourcecode.malls.util.AssertUtil;

@Service
@Transactional
public class OrderService implements BaseService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private SubOrderRepository subOrderRepository;

	@Autowired
	private ExpressRepository expressRepository;

	@Autowired
	protected EntityManager em;

	@Autowired
	private CacheClearer clearer;

	@Autowired
	private WechatService wechatService;

	@Autowired
	private AlipayService alipayService;

	@Transactional(readOnly = true)
	public Page<Order> getOrders(QueryInfo<OrderDTO> queryInfo) {
		Specification<Order> spec = new Specification<Order>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("merchant"), queryInfo.getData().getMerchantId()));
//				predicate.add(criteriaBuilder.equal(root.get("deleted"), false));
				if (queryInfo.getData() != null) {
					if (queryInfo.getData().getStartTime() != null) {
						predicate.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"),
								queryInfo.getData().getStartTime()));
					}
					if (queryInfo.getData().getEndTime() != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(queryInfo.getData().getEndTime());
						c.add(Calendar.DATE, 1);
						predicate.add(criteriaBuilder.lessThan(root.get("createTime"), c.getTime()));
					}
					if (Boolean.TRUE.equals(queryInfo.getData().getCancelForRefund())) {
//						predicate.add(criteriaBuilder.isNotNull(root.get("refundTime")));
//						predicate
//						.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("status"), OrderStatus.CanceledForRefund),
//								criteriaBuilder.equal(root.get("status"), OrderStatus.RefundApplied),
//								criteriaBuilder.equal(root.get("status"), OrderStatus.Refunded)));
						predicate.add(criteriaBuilder.equal(root.get("status"),
								OrderStatus.valueOf(queryInfo.getData().getStatusText())));
					}
					if (!StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
						String like = "%" + queryInfo.getData().getSearchText() + "%";
						predicate
								.add(criteriaBuilder.or(criteriaBuilder.like(root.join("client").get("username"), like),
										criteriaBuilder.like(root.get("orderId"), like)));
					}
					if (!"all".equalsIgnoreCase(queryInfo.getData().getStatusText())) {
						predicate.add(criteriaBuilder.equal(root.get("status"),
								OrderStatus.valueOf(queryInfo.getData().getStatusText())));
					}
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		return orderRepository.findAll(spec, queryInfo.getPage().pageable());
	}

	public void updateExpress(Long merchantId, OrderDTO dto) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(dto.getExpressList()), "请先编辑物流信息");
		Optional<Order> orderOp = orderRepository.findById(dto.getId());
		AssertUtil.assertTrue(orderOp.isPresent() && orderOp.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		Order order = orderOp.get();
		AssertUtil.assertTrue(!order.isDeleted(), "订单已被用户废弃，不能修改");
		AssertUtil.assertTrue(
				OrderStatus.Paid.equals(order.getStatus()) || OrderStatus.Shipped.equals(order.getStatus()),
				"不能修改物流信息");
		em.lock(order, LockModeType.PESSIMISTIC_WRITE);
		if (OrderStatus.Paid.equals(order.getStatus())) {
			order.setStatus(OrderStatus.Shipped);
			order.setSentTime(new Date());
			orderRepository.save(order);
			clearer.clearClientOrders(order);
		}
		if (!CollectionUtils.isEmpty(order.getExpressList())) {
			expressRepository.deleteAll(order.getExpressList());
		}
		for (ExpressDTO expressDTO : dto.getExpressList()) {
			Express express = expressDTO.asEntity();
			AssertUtil.assertNotNull(express.getType(), "必须选择快递类型");
			if (ExpressType.Delivery.equals(express.getType())) {
				AssertUtil.assertNotEmpty(express.getCompany(), "快递公司不能为空");
				AssertUtil.assertNotEmpty(express.getNumber(), "快递单号不能为空");
			}
			AssertUtil.assertTrue(!CollectionUtils.isEmpty(expressDTO.getSubList()), "物流信息没有包含商品");
			List<SubOrder> subList = new ArrayList<>();
			for (SubOrderDTO subDTO : expressDTO.getSubList()) {
				Optional<SubOrder> sub = subOrderRepository.findById(subDTO.getId());
				if (sub.isPresent()) {
					subList.add(sub.get());
				}
			}
			express.setSubList(subList);
			express.setMerchant(order.getMerchant());
			express.setClient(order.getClient());
			express.setOrder(order);
			expressRepository.save(express);
		}
	}

	public void approveRefund(Long merchantId, Long orderId) throws Exception {
		Optional<Order> orderOp = orderRepository.findById(orderId);
		AssertUtil.assertTrue(orderOp.isPresent() && orderOp.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		Order order = orderOp.get();
		AssertUtil.assertTrue(!order.isDeleted(), "订单已被用户废弃，不能修改");
		AssertUtil.assertTrue(OrderStatus.RefundApplied.equals(order.getStatus()), "状态有误，不能进行退款操作");
		em.lock(order, LockModeType.PESSIMISTIC_WRITE);
		// 自动退款
		switch (order.getPayment()) {
		case WePay: {
			WePayConfig config = wechatService.createWePayConfig(merchantId);
			wechatService.refund(config, order.getTransactionId(), order.getOrderId(), order.getRealPrice(),
					order.getRealPrice(), order.getSubList().size());
		}
			break;
		case AliPay: {
			alipayService.refund(merchantId, order.getTransactionId(), order.getOrderId(), order.getRealPrice(),
					order.getRealPrice(), order.getSubList().size());
		}
			break;
		default:
			throw new BusinessException("不支持的支付类型");
		}
		order.setStatus(OrderStatus.Refunded);
		order.setRefundTime(new Date());
		orderRepository.save(order);
		clearer.clearClientOrders(order);
	}

}
