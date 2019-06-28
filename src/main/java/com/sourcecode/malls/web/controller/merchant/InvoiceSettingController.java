package com.sourcecode.malls.web.controller.merchant;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.merchant.InvoiceSetting;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.merchant.InvoiceSettingDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.merchant.InvoiceSettingRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/invoice/setting")
public class InvoiceSettingController extends BaseController {

	@Autowired
	private InvoiceSettingRepository repository;

	@Autowired
	private MerchantRepository merchantRepository;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<InvoiceSettingDTO>> list(@RequestBody QueryInfo<SimpleQueryDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		Optional<Merchant> merchant = merchantRepository.findById(user.getId());
		Page<InvoiceSetting> result = null;
		if (queryInfo.getData() != null && !StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
			result = repository.findAllByMerchantAndContentLike(merchant.get(),
					"%" + queryInfo.getData().getSearchText() + "%", queryInfo.getPage().pageable());
		} else {
			result = repository.findAllByMerchant(merchant.get(), queryInfo.getPage().pageable());
		}
		PageResult<InvoiceSettingDTO> dtoResult = new PageResult<>(
				result.getContent().stream().map(data -> data.asDTO()).collect(Collectors.toList()),
				result.getTotalElements());
		return new ResultBean<>(dtoResult);
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<InvoiceSettingDTO> load(@PathVariable Long id) {
		return new ResultBean<>(check(id).asDTO());
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody InvoiceSettingDTO dto) {
		InvoiceSetting data = null;
		if (dto.getId() != null) {
			data = check(dto.getId());
		} else {
			data = new InvoiceSetting();
			User user = getRelatedCurrentUser();
			Optional<Merchant> merchant = merchantRepository.findById(user.getId());
			data.setMerchant(merchant.get());
		}
		BeanUtils.copyProperties(dto, data, "id");
		repository.save(data);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/delete/params/{id}")
	public ResultBean<Void> save(@PathVariable Long id) {
		InvoiceSetting data = check(id);
		repository.delete(data);
		return new ResultBean<>();
	}

	private InvoiceSetting check(Long id) {
		User user = getRelatedCurrentUser();
		Optional<InvoiceSetting> dataOp = repository.findById(id);
		AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(user.getId()),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return dataOp.get();
	}

}
