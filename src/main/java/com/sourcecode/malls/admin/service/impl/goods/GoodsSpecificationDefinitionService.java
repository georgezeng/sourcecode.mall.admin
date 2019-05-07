package com.sourcecode.malls.admin.service.impl.goods;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sourcecode.malls.admin.domain.goods.GoodsSpecificationDefinition;
import com.sourcecode.malls.admin.repository.jpa.impl.goods.GoodsSpecificationDefinitionRepository;
import com.sourcecode.malls.admin.service.impl.goods.base.BaseGoodsAttributeService;

@Service
@Transactional
public class GoodsSpecificationDefinitionService extends BaseGoodsAttributeService<GoodsSpecificationDefinition> {
	@Autowired
	private GoodsSpecificationDefinitionRepository definitionRepository;

	@Override
	public JpaRepository<GoodsSpecificationDefinition, Long> getRepository() {
		return definitionRepository;
	}

	@Override
	protected String getParentName() {
		return "group";
	}

	@Transactional(readOnly = true)
	public List<GoodsSpecificationDefinition> findAllByCategory(Long categoryId) {
		Specification<GoodsSpecificationDefinition> spec = new Specification<GoodsSpecificationDefinition>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<GoodsSpecificationDefinition> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("category"), categoryId));
				return query.where(predicate.toArray(new Predicate[] {})).distinct(true).getRestriction();
			}
		};
		return definitionRepository.findAll(spec);
	}

}
