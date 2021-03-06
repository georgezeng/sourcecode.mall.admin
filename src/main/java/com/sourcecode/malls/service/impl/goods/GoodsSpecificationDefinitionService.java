package com.sourcecode.malls.service.impl.goods;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sourcecode.malls.domain.goods.GoodsSpecificationDefinition;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsSpecificationDefinitionRepository;
import com.sourcecode.malls.service.impl.goods.base.BaseGoodsAttributeService;

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

}
