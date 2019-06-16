package com.sourcecode.malls.web.controller.goods;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import com.sourcecode.malls.domain.goods.GoodsItem;
import com.sourcecode.malls.domain.goods.GoodsItemPhoto;
import com.sourcecode.malls.domain.goods.GoodsItemRank;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.goods.GoodsItemDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsBrandRepository;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsCategoryRepository;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsItemRankRepository;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsItemRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.impl.goods.GoodsItemPropertyService;
import com.sourcecode.malls.service.impl.goods.GoodsItemService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/goods/item")
public class GoodsItemController extends BaseController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private GoodsItemRepository itemRepository;

	@Autowired
	private GoodsCategoryRepository categoryRepository;

	@Autowired
	private GoodsBrandRepository brandRepository;

	@Autowired
	private GoodsItemPropertyService propertyService;

	@Autowired
	private GoodsItemRankRepository rankRepository;

	@Autowired
	private GoodsItemService itemService;

	private String fileDir = "goods/item";

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<GoodsItemDTO>> list(@RequestBody QueryInfo<GoodsItemDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		queryInfo.getData().setMerchantId(user.getId());
		Page<GoodsItem> result = itemService.findAll(queryInfo);
		PageResult<GoodsItemDTO> dtoResult = new PageResult<>(
				result.getContent().stream().map(data -> data.asDTO(false, false)).collect(Collectors.toList()),
				result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsItemDTO> load(@PathVariable Long id) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(itemService.load(user.getId(), id));
	}

	@RequestMapping(path = "/save")
	public ResultBean<Long> save(@RequestBody GoodsItemDTO dto) {
		checkIfApplicationPassed("信息");
		if (dto.getId() == null) {
			dto.setId(0l);
		}
		User user = getRelatedCurrentUser();
		Merchant merchant = merchantRepository.findById(user.getId()).get();
		GoodsItem data = itemRepository.findById(dto.getId()).orElseGet(GoodsItem::new);
		BeanUtils.copyProperties(dto, data, "id", "merchant", "category", "brand", "photos", "properties", "enabled");
		data.setMerchant(merchant);
		AssertUtil.assertNotNull(dto.getCategoryId(), "必须选择商品分类");
		Optional<GoodsCategory> categoryOp = categoryRepository.findById(dto.getCategoryId());
		AssertUtil.assertTrue(categoryOp.isPresent(), "商品分类不存在");
		data.setCategory(categoryOp.get());
		AssertUtil.assertNotNull(dto.getBrandId(), "必须选择商品品牌");
		Optional<GoodsBrand> brandOp = brandRepository.findById(dto.getBrandId());
		AssertUtil.assertTrue(brandOp.isPresent(), "商品品牌不存在");
		data.setBrand(brandOp.get());
		if (dto.getMaxPrice() == null || dto.getMaxPrice().compareTo(BigDecimal.ZERO) == 0) {
			data.setMaxPrice(data.getMinPrice());
		}
		if (data.getId() == null) {
			data.setPhotos(null);
			itemRepository.save(data);
		}
		List<String> tmpPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		if (dto.getThumbnail() != null && dto.getThumbnail().startsWith("temp")) {
			String newPath = fileDir + "/" + user.getId() + "/" + data.getId() + "/thumb.png";
			String tmpPath = dto.getThumbnail();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setThumbnail(newPath);
		}
		List<GoodsItemPhoto> photos = data.getPhotos();
		if (photos == null) {
			photos = new ArrayList<>();
			data.setPhotos(photos);
		}
		int order = 0;
		for (Iterator<GoodsItemPhoto> it = photos.iterator(); it.hasNext();) {
			GoodsItemPhoto photo = it.next();
			String path = null;
			if (order < dto.getPhotos().size()) {
				path = dto.getPhotos().get(order);
			}
			if (path == null) {
				it.remove();
			} else if (path.startsWith("temp")) {
				String newPath = fileDir + "/" + user.getId() + "/" + data.getId() + "/photo/" + (order + 1) + ".png";
				newPaths.add(newPath);
				tmpPaths.add(path);
				photo.setPath(newPath);
				order++;
			} else if (!path.equals(photo.getPath())) {
				photo.setPath(path);
				order++;
			} else {
				order++;
			}
		}
		if (order < dto.getPhotos().size()) {
			for (int i = order; i < dto.getPhotos().size(); i++) {
				GoodsItemPhoto photo = new GoodsItemPhoto();
				photo.setOrder(i + 1);
				photo.setItem(data);
				String path = dto.getPhotos().get(i);
				String newPath = fileDir + "/" + user.getId() + "/" + data.getId() + "/photo/" + (order + 1) + ".png";
				newPaths.add(newPath);
				tmpPaths.add(path);
				photo.setPath(newPath);
				photos.add(photo);
			}
		}
		itemRepository.save(data);
		if (data.getRank() == null) {
			GoodsItemRank rank = new GoodsItemRank();
			rank.setItem(data);
			rankRepository.save(rank);
		}
		transfer(true, tmpPaths, newPaths);
		return new ResultBean<>(data.getId());
	}

	@RequestMapping(path = "/properties/save")
	public ResultBean<Void> saveProperties(@RequestBody GoodsItemDTO dto) {
		AssertUtil.assertNotNull(dto.getId(), "商品ID不存在");
		checkIfApplicationPassed("信息");
		User user = getRelatedCurrentUser();
		Optional<GoodsItem> item = itemRepository.findById(dto.getId());
		AssertUtil.assertTrue(item.isPresent() && item.get().getMerchant().getId().equals(user.getId()), "商品不存在");
		propertyService.save(item.get(), dto.getProperties());
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()),
				ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		User user = getRelatedCurrentUser();
		for (Long id : keys.getIds()) {
			Optional<GoodsItem> dataOp = itemService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				itemService.delete(dataOp.get());
			}
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/updateStatus/params/{status}")
	public ResultBean<Void> updateStatus(@RequestBody KeyDTO<Long> keys, @PathVariable Boolean status) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()),
				ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_UPDATE);
		User user = getRelatedCurrentUser();
		for (Long id : keys.getIds()) {
			Optional<GoodsItem> dataOp = itemService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				dataOp.get().setEnabled(status);
				if (status) {
					dataOp.get().setPutTime(new Date());
				}
				itemService.save(dataOp.get());
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
