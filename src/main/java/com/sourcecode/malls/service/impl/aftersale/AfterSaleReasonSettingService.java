package com.sourcecode.malls.service.impl.aftersale;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sourcecode.malls.domain.aftersale.AfterSaleReasonSetting;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.merchant.AfterSaleReasonSettingDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.AfterSaleType;
import com.sourcecode.malls.repository.jpa.impl.aftersale.AfterSaleReasonSettingRepository;
import com.sourcecode.malls.service.base.JpaService;

@Service
@Transactional
public class AfterSaleReasonSettingService implements JpaService<AfterSaleReasonSetting, Long> {
	@Autowired
	private AfterSaleReasonSettingRepository repository;

	public PageResult<AfterSaleReasonSettingDTO> getList(Long merchantId, QueryInfo<SimpleQueryDTO> queryInfo) {
		Specification<AfterSaleReasonSetting> spec = new Specification<AfterSaleReasonSetting>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<AfterSaleReasonSetting> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("merchant"), merchantId));
				if (queryInfo.getData() != null) {
					if (!StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
						String like = "%" + queryInfo.getData().getSearchText() + "%";
						predicate.add(criteriaBuilder.like(root.get("content"), like));
					}
					if (!StringUtils.isEmpty(queryInfo.getData().getStatusText())) {
						if (!"all".equals(queryInfo.getData().getStatusText())) {
							predicate.add(criteriaBuilder.equal(root.get("type"),
									AfterSaleType.valueOf(queryInfo.getData().getStatusText())));
						}
					}
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		Page<AfterSaleReasonSetting> result = repository.findAll(spec, queryInfo.getPage().pageable());
		return new PageResult<>(result.get().map(it -> it.asDTO()).collect(Collectors.toList()),
				result.getTotalElements());
	}

	@Override
	public JpaRepository<AfterSaleReasonSetting, Long> getRepository() {
		return repository;
	}
}
