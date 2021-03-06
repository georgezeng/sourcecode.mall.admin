package com.sourcecode.malls.service.impl.goods.base;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sourcecode.malls.domain.base.BaseGoodsAttribute;
import com.sourcecode.malls.domain.goods.GoodsSpecificationDefinition;
import com.sourcecode.malls.dto.goods.GoodsAttributeDTO;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.service.base.JpaService;

public abstract class BaseGoodsAttributeService<T extends BaseGoodsAttribute> implements JpaService<T, Long> {

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public Page<T> findAll(QueryInfo<GoodsAttributeDTO> queryInfo) {
		Page<T> pageResult = null;
		Specification<T> spec = new Specification<T>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				if (queryInfo.getData() != null) {
					if (queryInfo.getData().getParent() != null && queryInfo.getData().getParent().getId() > 0l) {
						if (getParentName().equals("group")) {
							if ("true".equals(queryInfo.getData().getStatusText())) {
								predicate.add(criteriaBuilder.equal(root.join("groups"), queryInfo.getData().getParent().getId()));
							} else if ("false".equals(queryInfo.getData().getStatusText())) {
								Subquery<GoodsSpecificationDefinition> subquery = query.subquery(GoodsSpecificationDefinition.class);
								Root<GoodsSpecificationDefinition> subRoot = subquery.from(GoodsSpecificationDefinition.class);
								subquery.select(subRoot.get("id"));
								subquery.where(criteriaBuilder.equal(subRoot.join("groups"), queryInfo.getData().getParent().getId()));
								predicate.add(criteriaBuilder.not(root.in(subquery)));
							}
						} 
						else if (queryInfo.getData().getParent() != null) {
							predicate.add(criteriaBuilder.equal(root.get(getParentName()), queryInfo.getData().getParent().getId()));
						}
					}
					if (queryInfo.getData().getLevel() > 0) {
						predicate.add(criteriaBuilder.equal(root.get("level"), queryInfo.getData().getLevel()));
					}
					predicate.add(criteriaBuilder.equal(root.get("merchant"), queryInfo.getData().getMerchantId()));
					String searchText = queryInfo.getData().getSearchText();
					if (!StringUtils.isEmpty(searchText)) {
						String like = "%" + searchText + "%";
						predicate.add(criteriaBuilder.like(root.get("name").as(String.class), like));
					}
					return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
				}
				return null;
			}
		};
		pageResult = ((JpaSpecificationExecutor<T>) getRepository()).findAll(spec, queryInfo.getPage().pageable());
		return pageResult;
	}

	protected abstract String getParentName();
}
