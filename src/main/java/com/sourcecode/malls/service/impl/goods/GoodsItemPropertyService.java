package com.sourcecode.malls.service.impl.goods;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.sourcecode.malls.domain.goods.GoodsItem;
import com.sourcecode.malls.domain.goods.GoodsItemProperty;
import com.sourcecode.malls.domain.goods.GoodsItemValue;
import com.sourcecode.malls.domain.goods.GoodsSpecificationValue;
import com.sourcecode.malls.dto.goods.GoodsAttributeDTO;
import com.sourcecode.malls.dto.goods.GoodsItemPropertyDTO;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsItemPropertyRepository;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsItemValueRepository;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsSpecificationValueRepository;
import com.sourcecode.malls.service.base.JpaService;
import com.sourcecode.malls.util.AssertUtil;

@Service
@Transactional
public class GoodsItemPropertyService implements JpaService<GoodsItemProperty, Long> {
	@Autowired
	private GoodsItemPropertyRepository repository;
	@Autowired
	private GoodsItemValueRepository valueRepository;
	@Autowired
	private GoodsSpecificationValueRepository specValueRepository;

	@Override
	public JpaRepository<GoodsItemProperty, Long> getRepository() {
		return repository;
	}

	public void save(GoodsItem item, List<GoodsItemPropertyDTO> list) {
		List<GoodsItemProperty> oldProperties = repository.findAllByItem(item);
		if (!CollectionUtils.isEmpty(oldProperties)) {
			for (GoodsItemProperty property : oldProperties) {
				valueRepository.deleteAll(valueRepository.findAllByUid(property.getUid()));
				repository.delete(property);
			}
		}
		if (!CollectionUtils.isEmpty(list)) {
			for (GoodsItemPropertyDTO dto : list) {
				AssertUtil.assertTrue(!CollectionUtils.isEmpty(dto.getValues()), "必须选择一个规格值");
				GoodsItemProperty data = dto.asEntity();
				data.setItem(item);
				repository.save(data);
				for (GoodsAttributeDTO valueDto : dto.getValues()) {
					Optional<GoodsSpecificationValue> valueOp = specValueRepository.findById(valueDto.getId());
					AssertUtil.assertTrue(valueOp.isPresent(), "规格值不存在");
					GoodsItemValue value = new GoodsItemValue();
					value.setValue(valueOp.get());
					value.setUid(data.getUid());
					valueRepository.save(value);
				}
			}
		}
	}

}
