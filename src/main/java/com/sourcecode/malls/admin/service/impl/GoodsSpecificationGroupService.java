package com.sourcecode.malls.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sourcecode.malls.admin.domain.goods.GoodsSpecificationGroup;
import com.sourcecode.malls.admin.repository.jpa.impl.GoodsSpecificationGroupRepository;
import com.sourcecode.malls.admin.service.base.BaseGoodsAttributeService;

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
