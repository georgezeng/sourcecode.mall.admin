package com.sourcecode.malls.admin.service.impl.goods;

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
import com.sourcecode.malls.admin.domain.goods.GoodsItem;
import com.sourcecode.malls.admin.dto.goods.GoodsItemDTO;
import com.sourcecode.malls.admin.dto.query.QueryInfo;
import com.sourcecode.malls.admin.repository.jpa.impl.goods.GoodsItemRepository;
import com.sourcecode.malls.admin.service.base.JpaService;

@Service
@Transactional
public class GoodsItemService implements JpaService<GoodsItem, Long> {
	@Autowired
	private GoodsItemRepository itemRepository;

	@Override
	public JpaRepository<GoodsItem, Long> getRepository() {
		return itemRepository;
	}

	@Transactional(readOnly = true)
	public Page<GoodsItem> findAll(QueryInfo<GoodsItemDTO> queryInfo) {
		Page<GoodsItem> pageReulst = null;
		Specification<GoodsItem> spec = new Specification<GoodsItem>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<GoodsItem> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				if (queryInfo.getData() != null) {
					predicate.add(criteriaBuilder.equal(root.get("merchant"), queryInfo.getData().getMerchantId()));
					String searchText = queryInfo.getData().getSearchText();
					if (!StringUtils.isEmpty(searchText)) {
						String like = "%" + searchText + "%";
						predicate.add(criteriaBuilder.or(criteriaBuilder.like(root.get("name").as(String.class), like),
								criteriaBuilder.like(root.get("code").as(String.class), like)));
					}
					if (!"all".equals(queryInfo.getData().getStatusText())) {
						predicate.add(criteriaBuilder.equal(root.get("enabled").as(boolean.class), Boolean.valueOf(queryInfo.getData().getStatusText())));
					}
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		pageReulst = itemRepository.findAll(spec, queryInfo.getPage().pageable());
		return pageReulst;
	}

}
