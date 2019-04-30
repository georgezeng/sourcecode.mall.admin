package com.sourcecode.malls.admin.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.admin.context.UserContext;
import com.sourcecode.malls.admin.domain.goods.GoodsBrand;
import com.sourcecode.malls.admin.domain.goods.GoodsCategory;
import com.sourcecode.malls.admin.domain.goods.GoodsItem;
import com.sourcecode.malls.admin.domain.goods.GoodsItemPhoto;
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.dto.base.KeyDTO;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.dto.goods.GoodsItemDTO;
import com.sourcecode.malls.admin.dto.query.PageResult;
import com.sourcecode.malls.admin.dto.query.QueryInfo;
import com.sourcecode.malls.admin.repository.jpa.impl.GoodsBrandRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.GoodsCategoryRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.GoodsItemRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantRepository;
import com.sourcecode.malls.admin.service.FileOnlineSystemService;
import com.sourcecode.malls.admin.service.impl.GoodsItemService;
import com.sourcecode.malls.admin.util.AssertUtil;

@RestController
@RequestMapping(path = "/goods/item")
public class GoodsItemController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private GoodsItemRepository itemRepository;

	@Autowired
	private GoodsCategoryRepository categoryRepository;

	@Autowired
	private GoodsBrandRepository brandRepository;

	@Autowired
	private GoodsItemService itemService;

	@Autowired
	private FileOnlineSystemService fileService;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<GoodsItemDTO>> list(@RequestBody QueryInfo<GoodsItemDTO> queryInfo) {
		User user = UserContext.get();
		Optional<Merchant> merchant = merchantRepository.findById(user.getId());
		queryInfo.getData().setMerchantId(merchant.get().getId());
		Page<GoodsItem> result = itemService.findAll(queryInfo);
		PageResult<GoodsItemDTO> dtoResult = new PageResult<>(result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()),
				result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsItemDTO> load(@PathVariable Long id) {
		User user = UserContext.get();
		Optional<GoodsItem> dataOp = itemRepository.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), "找不到记录");
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), "找不到记录");
		return new ResultBean<>(dataOp.get().asDTO());
	}

	@RequestMapping(path = "/update/params/{id}/{status}")
	public ResultBean<Void> updateStatus(@PathVariable Long id, @PathVariable Boolean status) {
		Optional<GoodsItem> dataOp = itemRepository.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), "商品不存在");
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(UserContext.get().getId()), "商品不存在");
		dataOp.get().setEnabled(status);
		itemRepository.save(dataOp.get());
		return new ResultBean<>();
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody GoodsItemDTO dto) {
		if (dto.getId() == null) {
			dto.setId(0l);
		}
		User user = UserContext.get();
		Merchant merchant = merchantRepository.findById(user.getId()).get();
		GoodsItem data = itemRepository.findById(dto.getId()).orElseGet(GoodsItem::new);
		BeanUtils.copyProperties(dto, data, "id", "merchant", "category", "brand", "photos");
		data.setMerchant(merchant);
		AssertUtil.assertNotNull(dto.getCategoryId(), "必须选择商品分类");
		Optional<GoodsCategory> categoryOp = categoryRepository.findById(dto.getCategoryId());
		AssertUtil.assertTrue(categoryOp.isPresent(), "商品分类不存在");
		data.setCategory(categoryOp.get());
		AssertUtil.assertNotNull(dto.getBrandId(), "必须选择商品品牌");
		Optional<GoodsBrand> brandOp = brandRepository.findById(dto.getBrandId());
		AssertUtil.assertTrue(brandOp.isPresent(), "商品品牌不存在");
		data.setBrand(brandOp.get());
		data.setPhotos(null);
		if (data.getId() == null) {
			itemRepository.save(data);
		}
		List<String> tmpPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		if (dto.getThumbnail() != null && dto.getThumbnail().startsWith("temp")) {
			String newPath = "goods/item/" + UserContext.get().getId() + "/" + data.getId() + "/thumb.png";
			String tmpPath = dto.getThumbnail();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setThumbnail(newPath);
		}
		List<GoodsItemPhoto> oldPhotos = data.getPhotos();
		int order = 0;
		for (String path : dto.getPhotos()) {
			GoodsItemPhoto photo = null;
			if (oldPhotos != null && order < oldPhotos.size()) {
				photo = oldPhotos.get(order);
			}
			if (photo == null || path.startsWith("temp")) {
				photo = new GoodsItemPhoto();
				String newPath = "goods/item/" + UserContext.get().getId() + "/" + data.getId() + "/photo" + (order + 1) + ".png";
				newPaths.add(newPath);
				tmpPaths.add(path);
				photo.setPath(newPath);
				photo.setOrder(order + 1);
				photo.setItem(data);
			}
			data.addPhoto(photo);
			order++;
		}
		itemRepository.save(data);
		for (int i = 0; i < newPaths.size(); i++) {
			String newPath = newPaths.get(i);
			String tmpPath = tmpPaths.get(i);
			byte[] buf = fileService.load(false, tmpPath);
			fileService.upload(true, newPath, new ByteArrayInputStream(buf));
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), "必须选择至少一条记录进行删除");
		for (Long id : keys.getIds()) {
			User user = UserContext.get();
			Optional<GoodsItem> dataOp = itemService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				itemService.delete(dataOp.get());
			}
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/updateStatus/params/{enabled}")
	public ResultBean<Void> updateStatus(@RequestBody KeyDTO<Long> keys, @PathVariable Boolean enabled) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), "必须选择至少一条记录进行更新");
		for (Long id : keys.getIds()) {
			User user = UserContext.get();
			Optional<GoodsItem> dataOp = itemService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				dataOp.get().setEnabled(enabled);
				itemService.save(dataOp.get());
			}
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/img/upload/params/{id}")
	public ResultBean<String> upload(@RequestParam("file") MultipartFile file, @PathVariable Long id) throws IOException {
		String dir = "" + (id.equals(0l) ? System.nanoTime() : id) + "/";
		String filePath = "temp/goods/item/" + UserContext.get().getId() + "/" + dir + System.nanoTime() + ".png";
		fileService.upload(false, filePath, file.getInputStream());
		return new ResultBean<>(filePath);
	}

	@RequestMapping(value = "/img/load")
	public Resource previewImg(@RequestParam String filePath) {
		AssertUtil.assertTrue(filePath.startsWith("temp/goods/item/" + UserContext.get().getId() + "/")
				|| filePath.startsWith("goods/item/" + UserContext.get().getId() + "/"), "图片路径不合法");
		if (filePath.startsWith("temp")) {
			return new ByteArrayResource(fileService.load(false, filePath));
		} else {
			return new ByteArrayResource(fileService.load(true, filePath));
		}
	}

}
