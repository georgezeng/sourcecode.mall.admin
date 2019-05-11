package com.sourcecode.malls.service.impl.goods;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sourcecode.malls.domain.goods.GoodsCategory;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsCategoryRepository;
import com.sourcecode.malls.service.impl.goods.base.BaseGoodsAttributeService;

@Service
@Transactional
public class GoodsCategoryService extends BaseGoodsAttributeService<GoodsCategory> {
	@Autowired
	private GoodsCategoryRepository categoryRepository;

	@Override
	public JpaRepository<GoodsCategory, Long> getRepository() {
		return categoryRepository;
	}

	@Override
	protected String getParentName() {
		return null;
	}

}
