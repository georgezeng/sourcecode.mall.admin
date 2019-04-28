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
import com.sourcecode.malls.admin.domain.goods.GoodsCategory;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.dto.base.KeyDTO;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.dto.merchant.GoodsAttributeDTO;
import com.sourcecode.malls.admin.dto.query.PageInfo;
import com.sourcecode.malls.admin.dto.query.QueryInfo;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantRepository;
import com.sourcecode.malls.admin.service.impl.GoodsCategoryService;
import com.sourcecode.malls.admin.util.AssertUtil;

@RestController
@RequestMapping(path = "/goods/category")
public class GoodsCategoryController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private GoodsCategoryService categoryService;

	@RequestMapping(path = "/list")
	public ResultBean<List<GoodsAttributeDTO>> list(@RequestBody QueryInfo<GoodsAttributeDTO> queryInfo) {
		User user = UserContext.get();
		Optional<Merchant> merchant = merchantRepository.findById(user.getId());
		queryInfo.getData().setMerchantId(merchant.get().getId());
		queryInfo.getData().setLevel(1);
		Page<GoodsCategory> result = categoryService.findAll(queryInfo);
		List<GoodsAttributeDTO> list = new ArrayList<>();
		result.getContent().stream().map(data -> data.asDTO(false, true)).forEach(data -> {
			list.add(data);
			appendSubList(list, data);
		});
		return new ResultBean<>(list);
	}

	private void appendSubList(List<GoodsAttributeDTO> list, GoodsAttributeDTO parent) {
		if (!CollectionUtils.isEmpty(parent.getAttrs())) {
			for (GoodsAttributeDTO sub : parent.getAttrs()) {
				list.add(sub);
				appendSubList(list, sub);
			}
		}
	}

	@RequestMapping(path = "/load/allParents")
	public ResultBean<List<GoodsAttributeDTO>> listAllParents() {
		QueryInfo<GoodsAttributeDTO> queryInfo = new QueryInfo<>();
		queryInfo.setData(new GoodsAttributeDTO());
		PageInfo page = new PageInfo();
		page.setNum(1);
		page.setSize(99999999);
		queryInfo.setPage(page);
		User user = UserContext.get();
		Optional<Merchant> merchant = merchantRepository.findById(user.getId());
		queryInfo.getData().setLeafLevel(3);
		queryInfo.getData().setMerchantId(merchant.get().getId());
		Page<GoodsCategory> result = categoryService.findAll(queryInfo);
		return new ResultBean<>(result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsAttributeDTO> load(@PathVariable Long id) {
		AssertUtil.assertNotNull(id, "找不到记录");
		User user = UserContext.get();
		Optional<GoodsCategory> dataOp = categoryService.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), "找不到记录");
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), "找不到记录");
		return new ResultBean<>(dataOp.get().asDTO(false, false));
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody GoodsAttributeDTO dto) {
		GoodsCategory data = new GoodsCategory();
		if (dto.getId() != null) {
			Optional<GoodsCategory> dataOp = categoryService.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), "找不到记录");
			AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(UserContext.get().getId()), "找不到记录");
			data = dataOp.get();
		} else {
			data.setMerchant(merchantRepository.findById(UserContext.get().getId()).get());
			if (dto.getLevel() > 1) {
				AssertUtil.assertNotNull(dto.getParent().getId(), "必须选择上级分类");
				Optional<GoodsCategory> parentOp = categoryService.findById(dto.getParent().getId());
				AssertUtil.assertTrue(parentOp.isPresent(), "找不到上级分类");
				data.setParent(parentOp.get());
			}
			data.setLevel(dto.getLevel());
		}
		data.setName(dto.getName());
		data.setOrder(dto.getOrder());
		categoryService.save(data);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), "必须选择至少一条记录进行删除");
		for (Long id : keys.getIds()) {
			User user = UserContext.get();
			Optional<GoodsCategory> dataOp = categoryService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				categoryService.delete(dataOp.get());
			}
		}
		return new ResultBean<>();
	}

}
