package com.sourcecode.malls.web.controller.setting;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.aftersale.AfterSaleReasonSetting;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.merchant.AfterSaleReasonSettingDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.impl.aftersale.AfterSaleReasonSettingService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/afterSale/reason/setting")
public class AfterSaleReasonSettingController extends BaseController {

	@Autowired
	private AfterSaleReasonSettingService service;

	@Autowired
	private MerchantRepository merchantRepository;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<AfterSaleReasonSettingDTO>> list(@RequestBody QueryInfo<SimpleQueryDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getList(user.getId(), queryInfo));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<AfterSaleReasonSettingDTO> load(@PathVariable Long id) {
		return new ResultBean<>(check(id).asDTO());
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody AfterSaleReasonSettingDTO dto) {
		AfterSaleReasonSetting data = null;
		if (dto.getId() != null) {
			data = check(dto.getId());
		} else {
			data = new AfterSaleReasonSetting();
			User user = getRelatedCurrentUser();
			Optional<Merchant> merchant = merchantRepository.findById(user.getId());
			data.setMerchant(merchant.get());
		}
		BeanUtils.copyProperties(dto, data, "id");
		service.save(data);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/delete/params/{id}")
	public ResultBean<Void> save(@PathVariable Long id) {
		AfterSaleReasonSetting data = check(id);
		service.delete(data);
		return new ResultBean<>();
	}

	private AfterSaleReasonSetting check(Long id) {
		User user = getRelatedCurrentUser();
		Optional<AfterSaleReasonSetting> dataOp = service.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return dataOp.get();
	}

}
