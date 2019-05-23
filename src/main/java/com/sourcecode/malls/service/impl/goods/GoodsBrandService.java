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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sourcecode.malls.domain.goods.GoodsBrand;
import com.sourcecode.malls.dto.goods.GoodsBrandDTO;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsBrandRepository;
import com.sourcecode.malls.service.base.JpaService;

@Service
@Transactional
public class GoodsBrandService implements JpaService<GoodsBrand, Long> {
	@Autowired
	private GoodsBrandRepository brandRepository;

	@Override
	public JpaRepository<GoodsBrand, Long> getRepository() {
		return brandRepository;
	}

	@Transactional(readOnly = true)
	public Page<GoodsBrand> findAll(QueryInfo<GoodsBrandDTO> queryInfo) {
		Page<GoodsBrand> pageReulst = null;
		Specification<GoodsBrand> spec = new Specification<GoodsBrand>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<GoodsBrand> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				if (queryInfo.getData() != null) {
					predicate.add(criteriaBuilder.equal(root.get("merchant"), queryInfo.getData().getMerchantId()));
					if (queryInfo.getData().getCategoryId() != null) {
						predicate.add(criteriaBuilder.equal(root.get("category"), queryInfo.getData().getCategoryId()));
					}
					String searchText = queryInfo.getData().getSearchText();
					if (!StringUtils.isEmpty(searchText)) {
						String like = "%" + searchText + "%";
						predicate.add(criteriaBuilder.like(root.get("name").as(String.class), like));
					}
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		pageReulst = brandRepository.findAll(spec, queryInfo.getPage().pageable());
		return pageReulst;
	}

}
