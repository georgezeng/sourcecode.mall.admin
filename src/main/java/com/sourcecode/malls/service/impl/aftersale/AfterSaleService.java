package com.sourcecode.malls.service.impl.aftersale;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.github.wxpay.sdk.WePayConfig;
import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.aftersale.AfterSaleApplication;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.dto.aftersale.AfterSaleApplicationDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.AfterSaleStatus;
import com.sourcecode.malls.enums.AfterSaleType;
import com.sourcecode.malls.exception.BusinessException;
import com.sourcecode.malls.repository.jpa.impl.aftersale.AfterSaleApplicationRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.base.BaseService;
import com.sourcecode.malls.service.impl.AlipayService;
import com.sourcecode.malls.service.impl.WechatService;
import com.sourcecode.malls.util.AssertUtil;

@Service
@Transactional
public class AfterSaleService implements BaseService {

	@Autowired
	private AfterSaleApplicationRepository applicationRepository;

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private WechatService wechatService;

	@Autowired
	private AlipayService alipayService;

	@Transactional(readOnly = true)
	public PageResult<AfterSaleApplicationDTO> getList(Long merchantId, QueryInfo<AfterSaleApplicationDTO> queryInfo) {
		Optional<Merchant> merchant = merchantRepository.findById(merchantId);
		Specification<AfterSaleApplication> spec = new Specification<AfterSaleApplication>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<AfterSaleApplication> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("merchant"), merchant.get()));
				if (queryInfo.getData() != null) {
					if (queryInfo.getData().getType() != null) {
						predicate.add(criteriaBuilder.equal(root.get("type"), queryInfo.getData().getType()));
					}
					if (!StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
						String like = "%" + queryInfo.getData().getSearchText() + "%";
						predicate.add(criteriaBuilder.or(criteriaBuilder.like(root.get("serviceId"), like),
								criteriaBuilder.like(root.join("client").get("username"), like)));
					}
					if (!StringUtils.isEmpty(queryInfo.getData().getStatusText())) {
						if (!"all".equals(queryInfo.getData().getStatusText())) {
							predicate.add(criteriaBuilder.equal(root.get("status"),
									AfterSaleStatus.valueOf(queryInfo.getData().getStatusText())));
						}
					}
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
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		Page<AfterSaleApplication> result = applicationRepository.findAll(spec, queryInfo.getPage().pageable());
		return new PageResult<>(result.get().map(it -> it.asDTO()).collect(Collectors.toList()),
				result.getTotalElements());
	}

	@Transactional(readOnly = true)
	public AfterSaleApplication load(Long merchantId, Long id) {
		Optional<AfterSaleApplication> data = applicationRepository.findById(id);
		AssertUtil.assertTrue(data.isPresent() && data.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return data.get();
	}

	public void refund(Long merchantId, AfterSaleApplicationDTO dto) throws Exception {
		AfterSaleApplication data = load(merchantId, dto.getId());
		AssertUtil.assertTrue(AfterSaleStatus.WaitForRefund.equals(data.getStatus()), "状态有误，不能对该申请记录进行退款操作");
		AssertUtil.assertTrue(!AfterSaleType.Change.equals(data.getType()), "该记录不能进行退款操作");
		AssertUtil.assertTrue(dto.getNums() > 0 && dto.getNums() <= data.getSubOrder().getNums(),
				"数量必须在1-" + data.getSubOrder().getNums() + "之间");
		AssertUtil.assertTrue(
				dto.getAmount() != null && dto.getAmount().compareTo(BigDecimal.ZERO) > 0
						&& dto.getAmount().compareTo(data.getSubOrder().getDealPrice()) <= 0,
				"金额必须在0.01-" + data.getSubOrder().getDealPrice() + "之间");
		data.setStatus(AfterSaleStatus.Finished);
		data.setRefundTime(new Date());
		data.setNums(dto.getNums());
		data.setAmount(dto.getAmount());
		data.setRemark(dto.getRemark());
		applicationRepository.save(data);
		switch (data.getOrder().getPayment()) {
		case WePay: {
			WePayConfig config = wechatService.createWePayConfig(merchantId);
			wechatService.refund(config, data.getOrder().getTransactionId(), data.getServiceId(),
					data.getOrder().getTotalPrice(), data.getAmount(), data.getOrder().getSubList().size());
		}
			break;
		case AliPay: {
			alipayService.refund(merchantId, data.getOrder().getTransactionId(), data.getServiceId(),
					data.getOrder().getTotalPrice(), data.getAmount(), data.getOrder().getSubList().size());
		}
			break;
		default:
			throw new BusinessException("不支持的支付类型");
		}
	}

	public void receive(Long merchantId, Long id) throws Exception {
		AfterSaleApplication data = load(merchantId, id);
		AssertUtil.assertTrue(AfterSaleStatus.WaitForReceive.equals(data.getStatus()), "状态有误，不能对该申请记录进行确认收货操作");
		AssertUtil.assertTrue(!AfterSaleType.RefundOnly.equals(data.getType()), "该记录不能进行确认收货操作");
		AfterSaleStatus status = null;
		switch (data.getType()) {
		case Change: {
			status = AfterSaleStatus.WaitForSend;
		}
			break;
		case SalesReturn: {
			status = AfterSaleStatus.WaitForRefund;
		}
			break;
		default:
			throw new BusinessException("暂不支持该类型的操作");
		}
		data.setStatus(status);
		data.setReceiveTime(new Date());
		applicationRepository.save(data);
	}

	public void sent(Long merchantId, AfterSaleApplicationDTO dto) throws Exception {
		AfterSaleApplication data = load(merchantId, dto.getId());
		AssertUtil.assertTrue(AfterSaleType.Change.equals(data.getType()), "该记录不能进行发货操作");
		AssertUtil.assertTrue(AfterSaleStatus.WaitForSend.equals(data.getStatus()), "状态有误，不能对该申请记录进行确认收货操作");
		AssertUtil.assertNotEmpty(dto.getMerchantExpressCompany(), "物流公司不能为空");
		AssertUtil.assertNotEmpty(dto.getMerchantExpressNumber(), "物流单号不能为空");
		data.setRemark(dto.getRemark());
		data.setMerchantExpressCompany(dto.getMerchantExpressCompany());
		data.setMerchantExpressNumber(dto.getMerchantExpressNumber());
		data.setSendTime(new Date());
		data.setStatus(AfterSaleStatus.WaitForPickup);
		applicationRepository.save(data);
	}
}
