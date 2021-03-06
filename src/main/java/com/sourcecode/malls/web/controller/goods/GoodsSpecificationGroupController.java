package com.sourcecode.malls.web.controller.goods;

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

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.goods.GoodsCategory;
import com.sourcecode.malls.domain.goods.GoodsSpecificationGroup;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.goods.GoodsAttributeDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsCategoryRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.impl.goods.GoodsSpecificationGroupService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/goods/specification/group")
public class GoodsSpecificationGroupController extends BaseController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private GoodsSpecificationGroupService groupService;

	@Autowired
	private GoodsCategoryRepository categoryRepository;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<GoodsAttributeDTO>> list(@RequestBody QueryInfo<GoodsAttributeDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		queryInfo.getData().setMerchantId(user.getId());
		Page<GoodsSpecificationGroup> result = groupService.findAll(queryInfo);
		PageResult<GoodsAttributeDTO> dtoResult = new PageResult<>(
				result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()), result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(path = "/listInCategory/params/{categoryId}")
	public ResultBean<GoodsAttributeDTO> listInCategory(@PathVariable Long categoryId) {
		List<GoodsSpecificationGroup> groups = new ArrayList<>();
		getList(groups, categoryId);
		return new ResultBean<>(groups.stream().map(data -> data.asDTO()).collect(Collectors.toList()));
	}

	private void getList(List<GoodsSpecificationGroup> groups, Long categoryId) {
		Optional<GoodsCategory> parentCategoryOp = categoryRepository.findById(categoryId);
		if (parentCategoryOp.isPresent()) {
			GoodsCategory parentCategory = parentCategoryOp.get();
			if (!CollectionUtils.isEmpty(parentCategory.getSubList())) {
				for (GoodsCategory sub : parentCategory.getSubList()) {
					getList(groups, sub.getId());
				}
			} else if (!CollectionUtils.isEmpty(parentCategory.getGroups())) {
				groups.addAll(parentCategory.getGroups());
			}
		}
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsAttributeDTO> load(@PathVariable Long id) {
		AssertUtil.assertNotNull(id, ExceptionMessageConstant.NO_SUCH_RECORD);
		User user = getRelatedCurrentUser();
		Optional<GoodsSpecificationGroup> dataOp = groupService.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
		return new ResultBean<>(dataOp.get().asDTO(true));
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody GoodsAttributeDTO dto) {
		User user = getRelatedCurrentUser();
		GoodsSpecificationGroup data = new GoodsSpecificationGroup();
		if (dto.getId() != null) {
			Optional<GoodsSpecificationGroup> dataOp = groupService.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
			AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
		} else {
			data.setMerchant(merchantRepository.findById(user.getId()).get());
		}
		AssertUtil.assertNotNull(dto.getParent(), "请选择一个商品分类");
		AssertUtil.assertNotNull(dto.getParent().getId(), "请选择一个商品分类");
		Optional<GoodsCategory> categoryOp = categoryRepository.findById(dto.getParent().getId());
		AssertUtil.assertTrue(categoryOp.isPresent(), "商品分类不存在");
		AssertUtil.assertTrue(categoryOp.get().getMerchant().getId().equals(data.getMerchant().getId()), "商品分类不存在");
		AssertUtil.assertTrue(categoryOp.get().getLevel() == 3, "必须是三级分类");
		data.setCategory(categoryOp.get());
		data.setName(dto.getName());
		data.setOrder(dto.getOrder());
		groupService.save(data);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		User user = getRelatedCurrentUser();
		for (Long id : keys.getIds()) {
			Optional<GoodsSpecificationGroup> dataOp = groupService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				groupService.delete(dataOp.get());
			}
		}
		return new ResultBean<>();
	}

}
