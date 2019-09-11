package com.sourcecode.malls.service.impl.aftersale;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.goods.GoodsItemEvaluation;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.dto.goods.GoodsItemEvaluationDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.GoodsItemEvaluationValue;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsItemEvaluationRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.impl.CacheClearer;
import com.sourcecode.malls.util.AssertUtil;

@Service
@Transactional
public class EvaluationService {
	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private GoodsItemEvaluationRepository repository;

	@Autowired
	private MerchantRepository merchantRepository;
	
	@Autowired
	private CacheClearer clearer;

	@Transactional(readOnly = true)
	public PageResult<GoodsItemEvaluationDTO> getList(Long merchantId, QueryInfo<GoodsItemEvaluationDTO> queryInfo) {
		Optional<Merchant> merchant = merchantRepository.findById(merchantId);
		AssertUtil.assertTrue(merchant.isPresent(), "找不到商家数据");
		Specification<GoodsItemEvaluation> spec = new Specification<GoodsItemEvaluation>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<GoodsItemEvaluation> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("merchant"), merchant.get()));
				if (queryInfo.getData() != null) {
					if (!StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
						String like = "%" + queryInfo.getData().getSearchText() + "%";
						predicate
								.add(criteriaBuilder.or(criteriaBuilder.like(root.join("client").get("username"), like),
										criteriaBuilder.like(root.join("subOrder").get("itemName"), like)));
					}
					String statusText = queryInfo.getData().getStatusText();
					if (!StringUtils.isEmpty(statusText) && !"All".equals(statusText)) {
						switch (statusText) {
						case "WaitForReply": {
							predicate.add(criteriaBuilder.equal(root.get("replied"), false));
							predicate.add(criteriaBuilder.equal(root.get("passed"), true));
						}
							break;
						case "HadReplied": {
							predicate.add(criteriaBuilder.equal(root.get("replied"), true));
						}
							break;
						case "Neutrality":
						case "Bad":
						case "Good": {
							predicate.add(criteriaBuilder.equal(root.get("value"),
									GoodsItemEvaluationValue.valueOf(statusText)));
						}
							break;
						case "HadAudit": {
							predicate.add(criteriaBuilder.equal(root.get("hasAudit"), true));
						}
							break;
						case "UnAudit": {
							predicate.add(criteriaBuilder.equal(root.get("hasAudit"), false));
						}
							break;
						case "Passed": {
							predicate.add(criteriaBuilder.equal(root.get("passed"), true));
						}
							break;
						case "UnPassed": {
							predicate.add(criteriaBuilder.equal(root.get("passed"), false));
							predicate.add(criteriaBuilder.equal(root.get("hasAudit"), true));
						}
							break;
						case "IsAdditional": {
							predicate.add(criteriaBuilder.equal(root.get("additional"), true));
						}
							break;
						case "NotAdditional": {
							predicate.add(criteriaBuilder.equal(root.get("additional"), false));
						}
							break;
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
		Page<GoodsItemEvaluation> result = repository.findAll(spec,
				queryInfo.getPage().pageable(Direction.DESC, "createTime"));
		return new PageResult<>(result.get().map(it -> {
			GoodsItemEvaluationDTO dto = it.asDTO(true);
			dto.setItemName(it.getSubOrder().getItemName());
			dto.setItemThumbnail(it.getSubOrder().getThumbnail());
			dto.setItemNums(it.getSubOrder().getNums());
			dto.setItemSpecificationValues(it.getSubOrder().getSpecificationValues());
			return dto;
		}).collect(Collectors.toList()), result.getTotalElements());
	}

	@Transactional(readOnly = true)
	public GoodsItemEvaluation load(Long merchantId, Long id) {
		AssertUtil.assertNotNull(id, "找不到评价序号");
		Optional<Merchant> merchant = merchantRepository.findById(merchantId);
		AssertUtil.assertTrue(merchant.isPresent(), "找不到商家数据");
		Optional<GoodsItemEvaluation> dataOp = repository.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent() && dataOp.get().getClient().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return dataOp.get();
	}

	public void reply(Long merchantId, GoodsItemEvaluationDTO dto) {
		GoodsItemEvaluation data = load(merchantId, dto.getId());
		if (!StringUtils.isEmpty(dto.getReply())) {
			data.setReply(dto.getReply());
			data.setReplied(true);
			data.setReplyTime(new Date());
		} else {
			data.setReplied(false);
			data.setReplyTime(null);
		}
		repository.save(data);
		clearer.clearEvaluation(data);
	}

	public void open(Long merchantId, GoodsItemEvaluationDTO dto) {
		GoodsItemEvaluation data = load(merchantId, dto.getId());
		AssertUtil.assertTrue(data.isHasAudit(), "请先审核再设置是否公开");
		data.setOpen(dto.isOpen());
		repository.save(data);
		clearer.clearEvaluation(data);
	}

	public void audit(Long merchantId, GoodsItemEvaluationDTO dto) {
		GoodsItemEvaluation data = load(merchantId, dto.getId());
		AssertUtil.assertTrue(!data.isHasAudit(), "不能修改审核状态");
		data.setPassed(dto.isPassed());
		if (dto.isPassed()) {
			data.setOpen(true);
		} else {
			data.setOpen(false);
		}
		data.setHasAudit(true);
		repository.save(data);
		clearer.clearEvaluation(data);
	}
}
