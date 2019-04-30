package com.sourcecode.malls.admin.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import com.sourcecode.malls.admin.domain.merchant.Merchant;
import com.sourcecode.malls.admin.domain.system.setting.User;
import com.sourcecode.malls.admin.dto.base.KeyDTO;
import com.sourcecode.malls.admin.dto.base.ResultBean;
import com.sourcecode.malls.admin.dto.goods.GoodsBrandDTO;
import com.sourcecode.malls.admin.dto.query.PageInfo;
import com.sourcecode.malls.admin.dto.query.PageResult;
import com.sourcecode.malls.admin.dto.query.QueryInfo;
import com.sourcecode.malls.admin.repository.jpa.impl.MerchantRepository;
import com.sourcecode.malls.admin.service.FileOnlineSystemService;
import com.sourcecode.malls.admin.service.impl.GoodsBrandService;
import com.sourcecode.malls.admin.util.AssertUtil;

@RestController
@RequestMapping(path = "/goods/brand")
public class GoodsBrandController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private GoodsBrandService brandService;

	@Autowired
	private FileOnlineSystemService fileService;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<GoodsBrandDTO>> list(@RequestBody QueryInfo<GoodsBrandDTO> queryInfo) {
		User user = UserContext.get();
		Optional<Merchant> merchant = merchantRepository.findById(user.getId());
		queryInfo.getData().setMerchantId(merchant.get().getId());
		Page<GoodsBrand> result = brandService.findAll(queryInfo);
		PageResult<GoodsBrandDTO> dtoResult = new PageResult<>(result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()),
				result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(path = "/list/all")
	public ResultBean<GoodsBrandDTO> listAll() {
		QueryInfo<GoodsBrandDTO> queryInfo = new QueryInfo<>();
		queryInfo.setData(new GoodsBrandDTO());
		PageInfo page = new PageInfo();
		page.setNum(1);
		page.setSize(99999999);
		queryInfo.setPage(page);
		User user = UserContext.get();
		Optional<Merchant> merchant = merchantRepository.findById(user.getId());
		queryInfo.getData().setMerchantId(merchant.get().getId());
		Page<GoodsBrand> result = brandService.findAll(queryInfo);
		return new ResultBean<>(result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<GoodsBrandDTO> load(@PathVariable Long id) {
		AssertUtil.assertNotNull(id, "找不到记录");
		User user = UserContext.get();
		Optional<GoodsBrand> dataOp = brandService.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), "找不到记录");
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), "找不到记录");
		return new ResultBean<>(dataOp.get().asDTO());
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody GoodsBrandDTO dto) {
		GoodsBrand data = new GoodsBrand();
		if (dto.getId() != null) {
			Optional<GoodsBrand> dataOp = brandService.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), "找不到记录");
			AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(UserContext.get().getId()), "找不到记录");
			data = dataOp.get();
			BeanUtils.copyProperties(dto, data, "merchant");
		} else {
			BeanUtils.copyProperties(dto, data, "merchant");
			data.setMerchant(merchantRepository.findById(UserContext.get().getId()).get());
		}

		String newPath = null;
		String tempPath = data.getLogo();
		if (tempPath != null && tempPath.startsWith("temp")) {
			newPath = "goods/brand/" + UserContext.get().getId() + "/" + System.nanoTime() + ".png";
			data.setLogo(newPath);
		}
		brandService.save(data);
		if (newPath != null) {
			byte[] buf = fileService.load(false, tempPath);
			fileService.upload(true, newPath, new ByteArrayInputStream(buf));
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), "必须选择至少一条记录进行删除");
		for (Long id : keys.getIds()) {
			User user = UserContext.get();
			Optional<GoodsBrand> dataOp = brandService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				brandService.delete(dataOp.get());
			}
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/logo/upload")
	public ResultBean<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
		String filePath = "temp/goods/brand/" + UserContext.get().getId() + "/" + System.nanoTime() + ".png";
		fileService.upload(false, filePath, file.getInputStream());
		return new ResultBean<>(filePath);
	}

	@RequestMapping(value = "/logo/load")
	public Resource previewImg(@RequestParam String filePath) {
		AssertUtil.assertTrue(filePath.startsWith("temp/goods/brand/" + UserContext.get().getId() + "/")
				|| filePath.startsWith("goods/brand/" + UserContext.get().getId() + "/"), "图片路径不合法");
		if (filePath.startsWith("temp")) {
			return new ByteArrayResource(fileService.load(false, filePath));
		} else {
			return new ByteArrayResource(fileService.load(true, filePath));
		}
	}

}
