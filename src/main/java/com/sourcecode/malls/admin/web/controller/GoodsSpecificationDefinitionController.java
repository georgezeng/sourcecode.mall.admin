package com.sourcecode.malls.admin.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.admin.context.UserContext;
import com.sourcecode.malls.admin.domain.goods.GoodsSpecificationDefinition;
import com.sourcecode.malls.admin.domain.goods.GoodsSpecificationGroup;
import com.sourcecode.malls.admin.domain.goods.GoodsSpecificationValue;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.dto.base.KeyDTO;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.dto.goods.GoodsAttributeDTO;
import com.sourcecode.malls.admin.dto.query.PageResult;
import com.sourcecode.malls.admin.dto.query.QueryInfo;
import com.sourcecode.malls.admin.repository.jpa.impl.GoodsSpecificationGroupRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.GoodsSpecificationValueRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantRepository;
import com.sourcecode.malls.admin.service.impl.GoodsSpecificationDefinitionService;
import com.sourcecode.malls.admin.util.AssertUtil;

@RestController
@RequestMapping(path = "/goods/specification/definition")
public class GoodsSpecificationDefinitionController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private GoodsSpecificationDefinitionService definitionService;

	@Autowired
	private GoodsSpecificationValueRepository valueRepository;

	@Autowired
	private GoodsSpecificationGroupRepository groupRepository;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<GoodsAttributeDTO>> list(@RequestBody QueryInfo<GoodsAttributeDTO> queryInfo) {
		User user = UserContext.get();
		Optional<Merchant> merchant = merchantRepository.findById(user.getId());
		queryInfo.getData().setMerchantId(merchant.get().getId());
		Page<GoodsSpecificationDefinition> result = definitionService.findAll(queryInfo);
		PageResult<GoodsAttributeDTO> dtoResult = new PageResult<>(
				result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()), result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(path = "/groups")
	public ResultBean<GoodsAttributeDTO> groups() {
		Optional<Merchant> merchant = merchantRepository.findById(UserContext.get().getId());
		List<GoodsSpecificationGroup> groups = groupRepository.findByMerchant(merchant.get());
		return new ResultBean<>(groups.stream().map(group -> group.asDTO()).collect(Collectors.toList()));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsAttributeDTO> load(@PathVariable Long id) {
		AssertUtil.assertNotNull(id, "找不到记录");
		User user = UserContext.get();
		Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), "找不到记录");
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), "找不到记录");
		return new ResultBean<>(dataOp.get().asDTO());
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody GoodsAttributeDTO dto) {
		GoodsSpecificationDefinition data = new GoodsSpecificationDefinition();
		if (dto.getId() != null) {
			Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), "找不到记录");
			AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(UserContext.get().getId()), "找不到记录");
			data = dataOp.get();
		} else {
			data.setMerchant(merchantRepository.findById(UserContext.get().getId()).get());
		}
		AssertUtil.assertNotNull(dto.getParent(), "请选择一个商品类型");
		AssertUtil.assertNotNull(dto.getParent().getId(), "请选择一个商品类型");
		Optional<GoodsSpecificationGroup> groupOp = groupRepository.findById(dto.getParent().getId());
		AssertUtil.assertTrue(groupOp.isPresent(), "商品类型不存在");
		AssertUtil.assertTrue(groupOp.get().getMerchant().getId().equals(data.getMerchant().getId()), "商品类型不存在");
		data.setGroup(groupOp.get());
		data.setName(dto.getName());
		data.setOrder(dto.getOrder());
		definitionService.save(data);
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(dto.getAttrs()), "至少需要编辑一个值属性");
		if (!CollectionUtils.isEmpty(data.getValues())) {
			valueRepository.deleteAll(data.getValues());
		}
		List<GoodsSpecificationValue> values = new ArrayList<>();
		int order = 1;
		for (GoodsAttributeDTO attr : dto.getAttrs()) {
			GoodsSpecificationValue value = new GoodsSpecificationValue();
			if (attr.getId() != null) {
				Optional<GoodsSpecificationValue> valueOp = valueRepository.findById(attr.getId());
				if (valueOp.isPresent()) {
					value = valueOp.get();
				}
			}
			value.setName(attr.getName());
			value.setMerchant(data.getMerchant());
			value.setOrder(order++);
			value.setDefinition(data);
			values.add(value);
		}
		valueRepository.saveAll(values);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), "必须选择至少一条记录进行删除");
		for (Long id : keys.getIds()) {
			User user = UserContext.get();
			Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				definitionService.delete(dataOp.get());
			}
		}
		return new ResultBean<>();
	}

}
