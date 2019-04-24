package com.sourcecode.malls.admin.service.impl;

import java.util.ArrayList;
import java.util.List;

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

import com.alibaba.druid.util.StringUtils;
import com.sourcecode.malls.admin.domain.goods.GoodsSpecificationDefinition;
import com.sourcecode.malls.admin.dto.merchant.GoodsAttributeDTO;
import com.sourcecode.malls.admin.dto.query.QueryInfo;
import com.sourcecode.malls.admin.repository.jpa.impl.GoodsSpecificationDefinitionRepository;
import com.sourcecode.malls.admin.service.base.JpaService;

@Service
@Transactional
public class GoodsSpecificationDefinitionService implements JpaService<GoodsSpecificationDefinition, Long> {
	@Autowired
	private GoodsSpecificationDefinitionRepository definitionRepository;

	@Transactional(readOnly = true)
	public Page<GoodsSpecificationDefinition> findAll(QueryInfo<GoodsAttributeDTO> queryInfo) {
		Page<GoodsSpecificationDefinition> pageReulst = null;
		Specification<GoodsSpecificationDefinition> spec = new Specification<GoodsSpecificationDefinition>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<GoodsSpecificationDefinition> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("merchant"), queryInfo.getData().getMerchant().getId()));
				String searchText = queryInfo.getData().getSearchText();
				if (!StringUtils.isEmpty(searchText)) {
					String like = "%" + searchText + "%";
					predicate.add(criteriaBuilder.like(root.get("name").as(String.class), like));
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		pageReulst = definitionRepository.findAll(spec, queryInfo.getPage().pageable());
		return pageReulst;
	}

	@Override
	public JpaRepository<GoodsSpecificationDefinition, Long> getRepository() {
		return definitionRepository;
	}

}
