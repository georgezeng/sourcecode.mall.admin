package com.sourcecode.malls.service.impl.goods;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sourcecode.malls.domain.goods.GoodsSpecificationGroup;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsSpecificationGroupRepository;
import com.sourcecode.malls.service.impl.goods.base.BaseGoodsAttributeService;

@Service
@Transactional
public class GoodsSpecificationGroupService extends BaseGoodsAttributeService<GoodsSpecificationGroup> {
	@Autowired
	private GoodsSpecificationGroupRepository groupRepository;

	@Override
	public JpaRepository<GoodsSpecificationGroup, Long> getRepository() {
		return groupRepository;
	}

	@Override
	protected String getParentName() {
		return "category";
	}

}
