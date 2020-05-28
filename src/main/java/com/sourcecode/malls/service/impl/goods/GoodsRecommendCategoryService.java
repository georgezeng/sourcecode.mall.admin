package com.sourcecode.malls.service.impl.goods;

import java.util.ArrayList;
import java.util.List;

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

import com.sourcecode.malls.domain.goods.GoodsRecommendCategory;
import com.sourcecode.malls.dto.goods.GoodsRecommendCategoryDTO;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsRecommendCategoryRepository;

@Service
@Transactional
public class GoodsRecommendCategoryService {
	@Autowired
	private GoodsRecommendCategoryRepository repository;

	public Page<GoodsRecommendCategory> findAll(QueryInfo<GoodsRecommendCategoryDTO> queryInfo) {
		Page<GoodsRecommendCategory> pageResult = null;
		Specification<GoodsRecommendCategory> spec = new Specification<GoodsRecommendCategory>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<GoodsRecommendCategory> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				if (queryInfo.getData() != null) {
					if (queryInfo.getData().getCategoryId() != null) {
						predicate.add(criteriaBuilder.equal(root.get("category"), queryInfo.getData().getCategoryId()));
					}
					predicate.add(criteriaBuilder.equal(root.get("merchant"), queryInfo.getData().getMerchantId()));
					String searchText = queryInfo.getData().getSearchText();
					if (!StringUtils.isEmpty(searchText)) {
						String like = "%" + searchText + "%";
						predicate.add(criteriaBuilder.like(root.get("name").as(String.class), like));
					}
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		pageResult = repository.findAll(spec, queryInfo.getPage().pageable());
		return pageResult;
	}

}
