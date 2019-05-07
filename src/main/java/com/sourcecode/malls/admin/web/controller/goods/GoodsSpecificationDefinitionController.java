package com.sourcecode.malls.admin.web.controller.goods;

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

import com.sourcecode.malls.admin.constants.ExceptionMessageConstant;
import com.sourcecode.malls.admin.domain.goods.GoodsCategory;
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
import com.sourcecode.malls.admin.repository.jpa.impl.goods.GoodsCategoryRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.goods.GoodsSpecificationGroupRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.goods.GoodsSpecificationValueRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.admin.service.impl.goods.GoodsSpecificationDefinitionService;
import com.sourcecode.malls.admin.util.AssertUtil;
import com.sourcecode.malls.admin.web.controller.base.BaseController;

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
		Optional<Merchant> merchant = merchantRepository.findById(user.getId());
		queryInfo.getData().setMerchantId(merchant.get().getId());
		Page<GoodsSpecificationDefinition> result = definitionService.findAll(queryInfo);
		PageResult<GoodsAttributeDTO> dtoResult = new PageResult<>(
				result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()), result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(path = "/listInCategory/params/{id}")
	public ResultBean<GoodsAttributeDTO> listInCategory(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		Optional<GoodsCategory> category = categoryRepository.findById(id);
		AssertUtil.assertTrue(category.isPresent() && category.get().getMerchant().getId().equals(user.getId()),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return new ResultBean<>(definitionService.findAllByCategory(id).stream().map(it -> it.asDTO()).collect(Collectors.toList()));
	}

	@RequestMapping(path = "/groups/params/{id}")
	public ResultBean<GoodsAttributeDTO> groups(@PathVariable Long id) {
		Optional<GoodsCategory> category = categoryRepository.findById(id);
		AssertUtil.assertTrue(category.isPresent(), "商品分类不存在");
		Optional<Merchant> merchant = merchantRepository.findById(getRelatedCurrentUser().getId());
		List<GoodsSpecificationGroup> groups = groupRepository.findByCategoryAndMerchant(category.get(), merchant.get());
		return new ResultBean<>(groups.stream().map(group -> group.asDTO()).collect(Collectors.toList()));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsAttributeDTO> load(@PathVariable Long id) {
		AssertUtil.assertNotNull(id, ExceptionMessageConstant.NO_SUCH_RECORD);
		User user = getRelatedCurrentUser();
		Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
		return new ResultBean<>(dataOp.get().asDTO());
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody GoodsAttributeDTO dto) {
		checkIfApplicationPassed("规格");
		User user = getRelatedCurrentUser();
		GoodsSpecificationDefinition data = new GoodsSpecificationDefinition();
		if (dto.getId() != null) {
			Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
			AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
		} else {
			data.setMerchant(merchantRepository.findById(user.getId()).get());
		}
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(dto.getParentIds()), "请选择一个商品类型");
		for (Long groupId : dto.getParentIds()) {
			Optional<GoodsSpecificationGroup> groupOp = groupRepository.findById(groupId);
			AssertUtil.assertTrue(groupOp.isPresent(), "商品类型不存在");
			AssertUtil.assertTrue(groupOp.get().getMerchant().getId().equals(data.getMerchant().getId()), "商品类型不存在");
			if (data.getCategory() == null) {
				data.setCategory(groupOp.get().getCategory());
			}
			boolean found = false;
			if (!CollectionUtils.isEmpty(data.getGroups())) {
				for (GoodsSpecificationGroup group : data.getGroups()) {
					if (group.getId().equals(groupId)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				data.addGroup(groupOp.get());
			}
		}
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

	@RequestMapping(value = "/delete/params/{groupId}")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys, @PathVariable Long groupId) {
		User user = getRelatedCurrentUser();
		if (groupId == null || groupId.equals(0l)) {
			AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
			for (Long id : keys.getIds()) {
				Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(id);
				if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
					definitionService.delete(dataOp.get());
				}
			}
			return new ResultBean<>();
		} else {
			AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_UPDATE);
			Optional<GoodsSpecificationGroup> groupOp = groupRepository.findById(groupId);
			AssertUtil.assertTrue(groupOp.isPresent() && groupOp.get().getMerchant().getId().equals(user.getId()),
					ExceptionMessageConstant.NO_SUCH_RECORD);
			GoodsSpecificationGroup group = groupOp.get();
			for (Long id : keys.getIds()) {
				Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(id);
				if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
					for (Iterator<GoodsSpecificationGroup> it = dataOp.get().getGroups().iterator(); it.hasNext();) {
						GoodsSpecificationGroup groupItem = it.next();
						if (group.getId().equals(groupItem.getId())) {
							it.remove();
							break;
						}
					}
					definitionService.save(dataOp.get());
				}
			}
			return new ResultBean<>();
		}
	}

	@RequestMapping(value = "/relate/params/{groupId}")
	public ResultBean<Void> relate(@RequestBody KeyDTO<Long> keys, @PathVariable Long groupId) {
		User user = getRelatedCurrentUser();
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_UPDATE);
		Optional<GoodsSpecificationGroup> groupOp = groupRepository.findById(groupId);
		AssertUtil.assertTrue(groupOp.isPresent() && groupOp.get().getMerchant().getId().equals(user.getId()),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		GoodsSpecificationGroup group = groupOp.get();
		List<GoodsAttributeDTO> list = listInCategory(group.getCategory().getId()).getDatas();
		for (GoodsAttributeDTO dto : list) {
			Optional<GoodsSpecificationDefinition> dataOp = definitionService.findById(dto.getId());
			if (keys.getIds().contains(dto.getId())) {
				boolean found = false;
				for (GoodsSpecificationGroup groupItem : dataOp.get().getGroups()) {
					if (groupItem.getId().equals(group.getId())) {
						found = true;
						break;
					}
				}
				if (!found) {
					dataOp.get().addGroup(group);
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
			definitionService.save(dataOp.get());
		}
		return new ResultBean<>();
	}

}
