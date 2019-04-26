package com.sourcecode.malls.admin.service.base;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.util.StringUtils;
import com.sourcecode.malls.admin.domain.base.BaseGoodsAttribute;
import com.sourcecode.malls.admin.dto.merchant.GoodsAttributeDTO;
import com.sourcecode.malls.admin.dto.query.QueryInfo;

public abstract class BaseGoodsAttributeService<T extends BaseGoodsAttribute> implements JpaService<T, Long> {

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public Page<T> findAll(QueryInfo<GoodsAttributeDTO> queryInfo) {
		Page<T> pageReulst = null;
		Specification<T> spec = new Specification<T>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				if (queryInfo.getData() != null && queryInfo.getData().getParent() != null && queryInfo.getData().getParent().getId() > 0l) {
					predicate.add(criteriaBuilder.equal(root.get(getParentName()), queryInfo.getData().getParent().getId()));
				}
				predicate.add(criteriaBuilder.equal(root.get("merchant"), queryInfo.getData().getMerchant()));
				String searchText = queryInfo.getData().getSearchText();
				if (!StringUtils.isEmpty(searchText)) {
					String like = "%" + searchText + "%";
					predicate.add(criteriaBuilder.like(root.get("name").as(String.class), like));
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		pageReulst = ((JpaSpecificationExecutor<T>) getRepository()).findAll(spec, queryInfo.getPage().pageable());
		return pageReulst;
	}

	protected abstract String getParentName();
}
