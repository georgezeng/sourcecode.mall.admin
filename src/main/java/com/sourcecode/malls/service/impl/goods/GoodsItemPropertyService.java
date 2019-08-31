package com.sourcecode.malls.service.impl.goods;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.sourcecode.malls.domain.goods.GoodsItem;
import com.sourcecode.malls.domain.goods.GoodsItemProperty;
import com.sourcecode.malls.domain.goods.GoodsSpecificationValue;
import com.sourcecode.malls.dto.goods.GoodsAttributeDTO;
import com.sourcecode.malls.dto.goods.GoodsItemPropertyDTO;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsItemPropertyRepository;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsSpecificationValueRepository;
import com.sourcecode.malls.service.base.JpaService;
import com.sourcecode.malls.util.AssertUtil;

@Service
@Transactional
public class GoodsItemPropertyService implements JpaService<GoodsItemProperty, Long> {
	@Autowired
	private GoodsItemPropertyRepository repository;
	@Autowired
	private GoodsSpecificationValueRepository specValueRepository;

	@Override
	public JpaRepository<GoodsItemProperty, Long> getRepository() {
		return repository;
	}

	public void save(GoodsItem item, List<GoodsItemPropertyDTO> list) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(list), "请至少选择一种规格");
		List<GoodsItemProperty> oldProperties = repository.findAllByItem(item);
		if (!CollectionUtils.isEmpty(list)) {
			List<GoodsItemProperty> used = new ArrayList<>();
			for (GoodsItemPropertyDTO dto : list) {
				GoodsItemProperty data = null;
				if (!CollectionUtils.isEmpty(oldProperties)) {
					for (GoodsItemProperty oldProperty : oldProperties) {
						if (oldProperty.getId().equals(dto.getId())) {
							data = oldProperty;
							used.add(oldProperty);
							break;
						}
					}
				}
				if (data == null) {
					data = dto.asEntity();
					data.setItem(item);
				} else {
					data.setInventory(dto.getInventory());
					data.setPrice(dto.getPrice());
				}
				List<GoodsSpecificationValue> values = new ArrayList<>();
				for (GoodsAttributeDTO valueDto : dto.getValues()) {
					Optional<GoodsSpecificationValue> valueOp = specValueRepository.findById(valueDto.getId());
					if (valueOp.isPresent()) {
						values.add(valueOp.get());
					}
				}
				data.setValues(values);
				repository.save(data);
			}
			if (!CollectionUtils.isEmpty(oldProperties)) {
				oldProperties.removeAll(used);
				repository.deleteAll(oldProperties);
			}
		}
	}

}
