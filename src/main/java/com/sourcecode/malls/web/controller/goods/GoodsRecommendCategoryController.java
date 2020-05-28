package com.sourcecode.malls.web.controller.goods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.goods.GoodsCategory;
import com.sourcecode.malls.domain.goods.GoodsRecommendCategory;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.goods.GoodsRecommendCategoryDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsCategoryRepository;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsRecommendCategoryRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.impl.goods.GoodsRecommendCategoryService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/goods/category/recommend")
public class GoodsRecommendCategoryController extends BaseController {

	@Autowired
	private GoodsCategoryRepository categoryRepository;

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private GoodsRecommendCategoryRepository repository;

	@Autowired
	private GoodsRecommendCategoryService service;

	private String fileDir = "goods/category/recommend";

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<GoodsRecommendCategoryDTO>> list(@RequestBody QueryInfo<GoodsRecommendCategoryDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		queryInfo.getData().setMerchantId(user.getId());
		Page<GoodsRecommendCategory> result = service.findAll(queryInfo);
		PageResult<GoodsRecommendCategoryDTO> dtoResult = new PageResult<>(
				result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()), result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsRecommendCategoryDTO> load(@PathVariable Long id) {
		AssertUtil.assertNotNull(id, ExceptionMessageConstant.NO_SUCH_RECORD);
		User user = getRelatedCurrentUser();
		Optional<GoodsRecommendCategory> dataOp = repository.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
		return new ResultBean<>(dataOp.get().asDTO());
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody GoodsRecommendCategoryDTO dto) {
		User user = getRelatedCurrentUser();
		GoodsRecommendCategory data = new GoodsRecommendCategory();
		if (dto.getId() != null) {
			Optional<GoodsRecommendCategory> dataOp = repository.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
			AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
		} else {
			data.setMerchant(merchantRepository.findById(user.getId()).get());
		}
		AssertUtil.assertNotNull(dto.getCategoryId(), "请选择一个商品分类");
		Optional<GoodsCategory> categoryOp = categoryRepository.findById(dto.getCategoryId());
		AssertUtil.assertTrue(categoryOp.isPresent(), "查找不到商品分类");
		GoodsCategory category = categoryOp.get();
		AssertUtil.assertTrue(category.getMerchant().getId().equals(data.getMerchant().getId()), "商品分类不存在");
		AssertUtil.assertIsNull(category.getParent(), "只能关联一级分类");
		data.setCategory(category);
		data.setName(dto.getName());
		data.setOrder(dto.getOrder());
		data.setLink(dto.getLink());
		data.setLogo(dto.getLogo());
		repository.save(data);
		List<String> tmpPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		if (dto.getLogo() != null && dto.getLogo().startsWith("temp")) {
			String newPath = fileDir + "/" + user.getId() + "/" + data.getId() + "/logo.png";
			String tmpPath = dto.getLogo();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setLogo(newPath);
		}
		transfer(true, tmpPaths, newPaths);
		repository.save(data);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		User user = getRelatedCurrentUser();
		for (Long id : keys.getIds()) {
			Optional<GoodsRecommendCategory> dataOp = repository.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				repository.delete(dataOp.get());
			}
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/file/upload/params/{id}")
	public ResultBean<String> upload(@RequestParam("file") MultipartFile file, @PathVariable Long id) throws IOException {
		return upload(file, fileDir, id, getRelatedCurrentUser().getId(), false);
	}

	@RequestMapping(value = "/file/load", produces = { MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public Resource load(@RequestParam String filePath) {
		return load(getRelatedCurrentUser().getId(), filePath, fileDir, true);
	}

}
