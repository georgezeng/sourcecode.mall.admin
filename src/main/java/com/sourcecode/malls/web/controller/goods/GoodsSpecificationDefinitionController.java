package com.sourcecode.malls.web.controller.goods;

import java.util.ArrayList;
import java.util.Iterator;
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

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.goods.GoodsCategory;
import com.sourcecode.malls.domain.goods.GoodsSpecificationDefinition;
import com.sourcecode.malls.domain.goods.GoodsSpecificationGroup;
import com.sourcecode.malls.domain.goods.GoodsSpecificationValue;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.goods.GoodsAttributeDTO;
import com.sourcecode.malls.dto.query.PageInfo;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsCategoryRepository;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsSpecificationGroupRepository;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsSpecificationValueRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.impl.goods.GoodsSpecificationDefinitionService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/goods/specification/definition")
public class GoodsSpecificationDefinitionController extends BaseController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private GoodsSpecificationDefinitionService definitionService;

	@Autowired
	private GoodsSpecificationValueRepository valueRepository;

	@Autowired
	private GoodsSpecificationGroupRepository groupRepository;

	@Autowired
	private GoodsCategoryRepository categoryRepository;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<GoodsAttributeDTO>> list(@RequestBody QueryInfo<GoodsAttributeDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		queryInfo.getData().setMerchantId(user.getId());
		Page<GoodsSpecificationDefinition> result = definitionService.findAll(queryInfo);
		PageResult<GoodsAttributeDTO> dtoResult = new PageResult<>(result.getContent().stream().map(data -> {
			GoodsAttributeDTO dto = data.asDTO();
			if (!CollectionUtils.isEmpty(data.getGroups()) && queryInfo.getData() != null
					&& queryInfo.getData().getParent() != null) {
				for (GoodsSpecificationGroup group : data.getGroups()) {
					if (group.getId().equals(queryInfo.getData().getParent().getId())) {
						dto.setEnabled(true);
						break;
					}
				}
			}
			return dto;
		}).collect(Collectors.toList()), result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(path = "/listInGroup/params/{groupId}")
	public ResultBean<GoodsAttributeDTO> list(@PathVariable Long groupId) {
		QueryInfo<GoodsAttributeDTO> queryInfo = new QueryInfo<>();
		queryInfo.setData(new GoodsAttributeDTO());
		PageInfo pageInfo = new PageInfo();
		pageInfo.setNum(1);
		pageInfo.setSize(999999999);
		queryInfo.setPage(pageInfo);
		User user = getRelatedCurrentUser();
		queryInfo.getData().setMerchantId(user.getId());
		GoodsAttributeDTO parent = new GoodsAttributeDTO();
		parent.setId(groupId);
		queryInfo.getData().setParent(parent);
		queryInfo.getData().setStatusText("true");
		Page<GoodsSpecificationDefinition> result = definitionService.findAll(queryInfo);
		return new ResultBean<>(result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()));
	}

	@RequestMapping(path = "/groups/params/{id}")
	public ResultBean<GoodsAttributeDTO> groups(@PathVariable Long id) {
		Optional<GoodsCategory> category = categoryRepository.findById(id);
		AssertUtil.assertTrue(category.isPresent(), "商品分类不存在");
		Optional<Merchant> merchant = merchantRepository.findById(getRelatedCurrentUser().getId());
		List<GoodsSpecificationGroup> groups = groupRepository.findByCategoryAndMerchant(category.get(),
				merchant.get());
		return new ResultBean<>(groups.stream().map(group -> group.asDTO()).collect(Collectors.toList()));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsAttributeDTO> load(@PathVariable Long id) {
		AssertUtil.assertNotNull(id, ExceptionMessageConstant.NO_SUCH_RECORD);
		User user = getRelatedCurrentUser();
		Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return new ResultBean<>(dataOp.get().asDTO());
	}

	@RequestMapping(path = "/save")
	public ResultBean<GoodsAttributeDTO> save(@RequestBody GoodsAttributeDTO dto) {
		User user = getRelatedCurrentUser();
		GoodsSpecificationDefinition data = new GoodsSpecificationDefinition();
		if (dto.getId() != null) {
			Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
			AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()),
					ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
		} else {
			data.setMerchant(merchantRepository.findById(user.getId()).get());
			Optional<GoodsSpecificationGroup> groupOp = groupRepository.findById(dto.getParent().getId());
			if (groupOp.isPresent()) {
				AssertUtil.assertTrue(groupOp.get().getMerchant().getId().equals(data.getMerchant().getId()),
						"商品类型不存在");
				data.addGroup(groupOp.get());
			}
		}
		data.setName(dto.getName());
		data.setOrder(dto.getOrder());
		definitionService.save(data);
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(dto.getAttrs()), "至少需要添加一个值");
//		if (!CollectionUtils.isEmpty(data.getValues())) {
//			valueRepository.deleteAll(data.getValues());
//		}
		List<GoodsSpecificationValue> values = new ArrayList<>();
		List<GoodsSpecificationValue> oldValues = new ArrayList<>();
		int order = 1;
		for (GoodsAttributeDTO attr : dto.getAttrs()) {
			GoodsSpecificationValue value = new GoodsSpecificationValue();
			if (attr.getId() != null) {
				Optional<GoodsSpecificationValue> valueOp = valueRepository.findById(attr.getId());
				if (valueOp.isPresent()) {
					value = valueOp.get();
					for (GoodsSpecificationValue oldValue : data.getValues()) {
						if (value.getId().equals(oldValue.getId())) {
							oldValues.add(oldValue);
							break;
						}
					}
				}
			}
			value.setName(attr.getName());
			value.setMerchant(data.getMerchant());
			value.setOrder(order++);
			value.setDefinition(data);
			values.add(value);
		}
		if (!oldValues.isEmpty()) {
			data.getValues().removeAll(oldValues);
			valueRepository.deleteAll(data.getValues());
		}
		valueRepository.saveAll(values);
		return load(data.getId());
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		User user = getRelatedCurrentUser();
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()),
				ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		for (Long id : keys.getIds()) {
			Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				definitionService.delete(dataOp.get());
			}
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/relate/params/{groupId}/{status}")
	public ResultBean<Void> relate(@RequestBody KeyDTO<Long> keys, @PathVariable Long groupId,
			@PathVariable Boolean status) {
		User user = getRelatedCurrentUser();
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()),
				ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_UPDATE);
		Optional<GoodsSpecificationGroup> groupOp = groupRepository.findById(groupId);
		AssertUtil.assertTrue(groupOp.isPresent() && groupOp.get().getMerchant().getId().equals(user.getId()),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		GoodsSpecificationGroup group = groupOp.get();
		for (Long id : keys.getIds()) {
			Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(id);
			if (dataOp.isPresent()) {
				GoodsSpecificationDefinition data = dataOp.get();
				if (status) {
					boolean found = false;
					for (GoodsSpecificationGroup groupItem : data.getGroups()) {
						if (groupItem.getId().equals(group.getId())) {
							found = true;
							break;
						}
					}
					if (!found) {
						data.addGroup(group);
					}
				} else {
					for (Iterator<GoodsSpecificationGroup> it = dataOp.get().getGroups().iterator(); it.hasNext();) {
						GoodsSpecificationGroup groupItem = it.next();
						if (group.getId().equals(groupItem.getId())) {
							it.remove();
							break;
						}
					}
				}
				definitionService.save(data);
			}
		}
		return new ResultBean<>();
	}

}
