package com.sourcecode.malls.service.impl.goods;

import java.util.ArrayList;
import java.util.Calendar;
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

import com.sourcecode.malls.domain.goods.GoodsItem;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.order.SubOrderRepository;
import com.sourcecode.malls.service.base.BaseService;
import com.sourcecode.malls.service.base.JpaService;
import com.sourcecode.malls.service.impl.BaseGoodsItemService;
import com.sourcecode.malls.util.AssertUtil;

@Service
@Transactional
public class GoodsItemService extends BaseGoodsItemService implements BaseService, JpaService<GoodsItem, Long> {

	@Autowired
	private SubOrderRepository subOrderRepository;

	@Override
	public JpaRepository<GoodsItem, Long> getRepository() {
		return itemRepository;
	}

	@Transactional(readOnly = true)
	public Page<GoodsItem> findAll(Long merchantId, QueryInfo<SimpleQueryDTO> queryInfo) {
		Page<GoodsItem> pageResult = null;
		Specification<GoodsItem> spec = new Specification<GoodsItem>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<GoodsItem> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("merchant"), merchantId));
				if (queryInfo.getData() != null) {
					String searchText = queryInfo.getData().getSearchText();
					if (!StringUtils.isEmpty(searchText)) {
						String like = "%" + searchText + "%";
						predicate.add(criteriaBuilder.or(criteriaBuilder.like(root.get("name").as(String.class), like),
								criteriaBuilder.like(root.get("number").as(String.class), like), criteriaBuilder.like(root.get("code").as(String.class), like)));
					}
					if (!StringUtils.isEmpty(queryInfo.getData().getStatusText())) {
						if (!"all".equals(queryInfo.getData().getStatusText())) {
							String[] strs = queryInfo.getData().getStatusText().split("-");
							if ("status".equals(strs[0])) {
								predicate.add(criteriaBuilder.equal(root.get("enabled"), Boolean.valueOf(strs[1])));
							} else if ("special".equals(strs[0])) {
								predicate.add(criteriaBuilder.equal(root.get("special"), Boolean.valueOf(strs[1])));
							} else if ("index".equals(strs[0])) {
								predicate.add(criteriaBuilder.equal(root.get("indexRecommend"), Integer.valueOf(strs[1])));
							}
						}
					}
					if (queryInfo.getData().getStartTime() != null) {
						predicate.add(criteriaBuilder.greaterThanOrEqualTo(root.get("putTime"), queryInfo.getData().getStartTime()));
					}
					if (queryInfo.getData().getEndTime() != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(queryInfo.getData().getEndTime());
						c.add(Calendar.DATE, 1);
						predicate.add(criteriaBuilder.lessThan(root.get("putTime"), c.getTime()));
					}
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		pageResult = itemRepository.findAll(spec, queryInfo.getPage().pageable());
		return pageResult;
	}

	public void delete(GoodsItem item) {
		AssertUtil.assertTrue(!item.isEnabled(), "商品上架中，不能删除");
		AssertUtil.assertTrue(subOrderRepository.countByItem(item) == 0, "已有订单关联，不能删除商品");
		itemRepository.delete(item);
	}

}
