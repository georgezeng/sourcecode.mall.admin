package com.sourcecode.malls.web.controller.goods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
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

import com.sourcecode.malls.constants.EnvConstant;
import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.goods.GoodsCategory;
import com.sourcecode.malls.domain.goods.GoodsSpecificationGroup;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.goods.GoodsAttributeDTO;
import com.sourcecode.malls.dto.query.PageInfo;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsSpecificationGroupRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.impl.CacheEvictService;
import com.sourcecode.malls.service.impl.goods.GoodsCategoryService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/goods/category")
public class GoodsCategoryController extends BaseController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private GoodsCategoryService categoryService;

	@Autowired
	private GoodsSpecificationGroupRepository groupRepository;

	@Autowired
	private CacheEvictService cacheEvictService;

	private String fileDir = "goods/category";

	@Autowired
	private Environment env;

	@RequestMapping(path = "/list")
	public ResultBean<GoodsAttributeDTO> list(@RequestBody QueryInfo<GoodsAttributeDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		queryInfo.getData().setMerchantId(user.getId());
		queryInfo.getData().setLevel(1);
		Page<GoodsCategory> result = categoryService.findAll(queryInfo);
		List<GoodsAttributeDTO> list = new ArrayList<>();
		result.getContent().stream().map(data -> data.asDTO(false, true)).forEach(data -> {
			list.add(data);
			appendSubList(list, data, true);
		});
		return new ResultBean<>(list);
	}

	private void appendSubList(List<GoodsAttributeDTO> list, GoodsAttributeDTO parent, boolean withLeaf) {
		if (!CollectionUtils.isEmpty(parent.getAttrs())) {
			for (GoodsAttributeDTO sub : parent.getAttrs()) {
				list.add(sub);
				if (sub.getLevel() == 2 && withLeaf) {
					appendSubList(list, sub, withLeaf);
				}
			}
		}
	}

	@RequestMapping(path = "/list/allParents")
	public ResultBean<GoodsAttributeDTO> listAllParents() {
		QueryInfo<GoodsAttributeDTO> queryInfo = new QueryInfo<>();
		queryInfo.setData(new GoodsAttributeDTO());
		PageInfo page = new PageInfo();
		page.setNum(1);
		page.setSize(99999999);
		queryInfo.setPage(page);
		User user = getRelatedCurrentUser();
		queryInfo.getData().setLevel(1);
		queryInfo.getData().setMerchantId(user.getId());
		Page<GoodsCategory> result = categoryService.findAll(queryInfo);
		List<GoodsAttributeDTO> list = new ArrayList<>();
		result.getContent().stream().map(data -> data.asDTO(false, true)).forEach(data -> {
			list.add(data);
			appendSubList(list, data, false);
		});
		return new ResultBean<>(list);
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsAttributeDTO> load(@PathVariable Long id) {
		AssertUtil.assertNotNull(id, ExceptionMessageConstant.NO_SUCH_RECORD);
		User user = getRelatedCurrentUser();
		Optional<GoodsCategory> dataOp = categoryService.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return new ResultBean<>(dataOp.get().asDTO(false, false));
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody GoodsAttributeDTO dto) {
		GoodsCategory data = new GoodsCategory();
		User user = getRelatedCurrentUser();
		if (dto.getId() != null) {
			Optional<GoodsCategory> dataOp = categoryService.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
			AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()),
					ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
		} else {
			data.setMerchant(merchantRepository.findById(user.getId()).get());
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
		if (data.getId() == null) {
			data.setIcon(dto.getIcon());
			categoryService.save(data);
		}
		List<String> tmpPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		if (dto.getIcon() != null && dto.getIcon().startsWith("temp")) {
			String newPath = fileDir + "/" + user.getId() + "/" + data.getId();
			if (env.acceptsProfiles(Profiles.of(EnvConstant.LOCAL))) {
				newPath += "/icon_" + System.currentTimeMillis() + ".png";
			} else {
				newPath += "/icon.png";
			}
			String tmpPath = dto.getIcon();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setIcon(newPath);
		}
		categoryService.save(data);
		if (data.getLevel() == 1) {
			cacheEvictService.clearGoodsCategoryLevel1(user.getId());
		} else if (data.getLevel() == 2) {
			cacheEvictService.clearGoodsCategoryLevel2(data.getParent().getId());
		}
		transfer(true, tmpPaths, newPaths);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()),
				ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		User user = getRelatedCurrentUser();
		for (Long id : keys.getIds()) {
			Optional<GoodsCategory> dataOp = categoryService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				AssertUtil.assertTrue(CollectionUtils.isEmpty(dataOp.get().getSubList()), "请先删除所有子类");
				if (!CollectionUtils.isEmpty(dataOp.get().getGroups())) {
					for (GoodsSpecificationGroup group : dataOp.get().getGroups()) {
						group.setCategory(null);
					}
					groupRepository.saveAll(dataOp.get().getGroups());
				}
				categoryService.delete(dataOp.get());
			}
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/file/upload/params/{id}")
	public ResultBean<String> upload(@RequestParam("file") MultipartFile file, @PathVariable Long id)
			throws IOException {
		return upload(file, fileDir, id, getRelatedCurrentUser().getId(), false);
	}

	@RequestMapping(value = "/file/load", produces = { MediaType.IMAGE_PNG_VALUE,
			MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public Resource load(@RequestParam String filePath) {
		return load(getRelatedCurrentUser().getId(), filePath, fileDir, true);
	}

}
