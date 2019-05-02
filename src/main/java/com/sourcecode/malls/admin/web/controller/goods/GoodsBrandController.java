package com.sourcecode.malls.admin.web.controller.goods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.admin.constants.ExceptionMessageConstant;
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
import com.sourcecode.malls.admin.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.admin.repository.jpa.impl.merchant.MerchantShopApplicationRepository;
import com.sourcecode.malls.admin.service.FileOnlineSystemService;
import com.sourcecode.malls.admin.service.impl.goods.GoodsBrandService;
import com.sourcecode.malls.admin.util.AssertUtil;
import com.sourcecode.malls.admin.web.controller.base.BaseFileOperationController;
import com.sourcecode.malls.admin.web.controller.base.BaseGoodsController;

@RestController
@RequestMapping(path = "/goods/brand")
public class GoodsBrandController implements BaseFileOperationController, BaseGoodsController {

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private MerchantShopApplicationRepository applicationRepository;

	@Autowired
	private GoodsBrandService brandService;

	@Autowired
	private FileOnlineSystemService fileService;

	private String fileDir = "goods/brand";

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
		AssertUtil.assertNotNull(id, ExceptionMessageConstant.NO_SUCH_RECORD);
		User user = UserContext.get();
		Optional<GoodsBrand> dataOp = brandService.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
		return new ResultBean<>(dataOp.get().asDTO());
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody GoodsBrandDTO dto) {
		checkIfApplicationPassed(applicationRepository, "品牌");
		GoodsBrand data = null;
		if (dto.getId() != null) {
			Optional<GoodsBrand> dataOp = brandService.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
			AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(UserContext.get().getId()), ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
			BeanUtils.copyProperties(dto, data, "merchant");
		} else {
			data = new GoodsBrand();
			BeanUtils.copyProperties(dto, data, "merchant");
			data.setMerchant(merchantRepository.findById(UserContext.get().getId()).get());
		}
		if (data.getId() == null) {
			brandService.save(data);
		}
		List<String> tmpPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		if (dto.getLogo() != null && dto.getLogo().startsWith("temp")) {
			String newPath = fileDir + "/" + UserContext.get().getId() + "/" + data.getId() + "/logo.png";
			String tmpPath = dto.getLogo();
			newPaths.add(newPath);
			tmpPaths.add(tmpPath);
			data.setLogo(newPath);
		}
		brandService.save(data);
		transfer(fileService, true, tmpPaths, newPaths);
		return new ResultBean<>();
	}

	@RequestMapping(value = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> keys) {
		AssertUtil.assertTrue(!CollectionUtils.isEmpty(keys.getIds()), ExceptionMessageConstant.SELECT_AT_LEAST_ONE_TO_DELETE);
		for (Long id : keys.getIds()) {
			User user = UserContext.get();
			Optional<GoodsBrand> dataOp = brandService.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(user.getId())) {
				brandService.delete(dataOp.get());
			}
		}
		return new ResultBean<>();
	}

	@RequestMapping(value = "/file/upload/params/{id}")
	public ResultBean<String> upload(@RequestParam("file") MultipartFile file, @PathVariable Long id) throws IOException {
		Optional<GoodsBrand> data = brandService.findById(id);
		AssertUtil.assertTrue(data.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		return upload(fileService, file, fileDir, id, UserContext.get().getId(), false);
	}

	@RequestMapping(value = "/file/load")
	public Resource load(@RequestParam String filePath) {
		return load(fileService, UserContext.get().getId(), filePath, fileDir, true);
	}

}
