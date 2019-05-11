package com.sourcecode.malls.web.controller.goods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
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
import com.sourcecode.malls.domain.goods.GoodsBrand;
import com.sourcecode.malls.domain.goods.GoodsCategory;
import com.sourcecode.malls.domain.system.setting.User;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.goods.GoodsBrandDTO;
import com.sourcecode.malls.dto.query.PageInfo;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsCategoryRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.impl.goods.GoodsBrandService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/goods/brand")
public class GoodsBrandController extends BaseController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private GoodsCategoryRepository categoryRepository;

	@Autowired
	private GoodsBrandService brandService;

	private String fileDir = "goods/brand";

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<GoodsBrandDTO>> list(@RequestBody QueryInfo<GoodsBrandDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		queryInfo.getData().setMerchantId(user.getId());
		Page<GoodsBrand> result = brandService.findAll(queryInfo);
		PageResult<GoodsBrandDTO> dtoResult = new PageResult<>(result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()),
				result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(path = "/listInCategory/params/{categoryId}")
	public ResultBean<GoodsBrandDTO> listInCategory(@PathVariable Long categoryId) {
		QueryInfo<GoodsBrandDTO> queryInfo = new QueryInfo<>();
		queryInfo.setData(new GoodsBrandDTO());
		PageInfo page = new PageInfo();
		page.setNum(1);
		page.setSize(99999999);
		queryInfo.setPage(page);
		queryInfo.getData().setCategoryId(categoryId);
		User user = getRelatedCurrentUser();
		queryInfo.getData().setMerchantId(user.getId());
		Page<GoodsBrand> result = brandService.findAll(queryInfo);
		return new ResultBean<>(result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsBrandDTO> load(@PathVariable Long id) {
		AssertUtil.assertNotNull(id, ExceptionMessageConstant.NO_SUCH_RECORD);
		User user = getRelatedCurrentUser();
		Optional<GoodsBrand> dataOp = brandService.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
		return new ResultBean<>(dataOp.get().asDTO());
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody GoodsBrandDTO dto) {
		checkIfApplicationPassed("品牌");
		GoodsBrand data = null;
		User user = getRelatedCurrentUser();
		if (dto.getId() != null) {
			Optional<GoodsBrand> dataOp = brandService.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
			AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
			BeanUtils.copyProperties(dto, data, "merchant");
		} else {
			data = new GoodsBrand();
			BeanUtils.copyProperties(dto, data, "merchant");
			data.setMerchant(merchantRepository.findById(user.getId()).get());
		}
		AssertUtil.assertNotNull(dto.getCategoryId(), "商品分类不能为空");
		Optional<GoodsCategory> categoryOp = categoryRepository.findById(dto.getCategoryId());
		AssertUtil.assertTrue(categoryOp.isPresent(), "商品分类不存在");
		GoodsCategory category = categoryOp.get();
		AssertUtil.assertTrue(category.getMerchant().getId().equals(getRelatedCurrentUser().getId()), "商品分类不存在");
		AssertUtil.assertTrue(category.getLevel() == 3, "必须是三级分类");
		data.setCategory(category);
		if (data.getId() == null) {
			brandService.save(data);
		}
		List<String> tmpPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		if (dto.getLogo() != null && dto.getLogo().startsWith("temp")) {
			String newPath = fileDir + "/" + user.getId() + "/" + data.getId() + "/logo.png";
			String tmpPath = dto.getLogo();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setLogo(newPath);
		}
		brandService.save(data);
		transfer(true, tmpPaths, newPaths);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		User user = getRelatedCurrentUser();
		for (Long id : keys.getIds()) {
			Optional<GoodsBrand> dataOp = brandService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				brandService.delete(dataOp.get());
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
