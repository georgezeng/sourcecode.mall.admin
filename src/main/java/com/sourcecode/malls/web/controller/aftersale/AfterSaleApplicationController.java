package com.sourcecode.malls.web.controller.aftersale;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcecode.malls.constants.MerchantSettingConstant;
import com.sourcecode.malls.domain.aftersale.AfterSaleApplication;
import com.sourcecode.malls.domain.aftersale.AfterSaleReturnAddress;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.domain.merchant.MerchantSetting;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.aftersale.AfterSaleApplicationDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.client.ClientAddressDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.AfterSaleStatus;
import com.sourcecode.malls.exception.BusinessException;
import com.sourcecode.malls.repository.jpa.impl.aftersale.AfterSaleApplicationRepository;
import com.sourcecode.malls.repository.jpa.impl.aftersale.AfterSaleReturnAddressRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantSettingRepository;
import com.sourcecode.malls.service.impl.CacheClearer;
import com.sourcecode.malls.service.impl.CacheEvictService;
import com.sourcecode.malls.service.impl.aftersale.AfterSaleService;
import com.sourcecode.malls.util.AssertUtil;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/afterSale/application")
public class AfterSaleApplicationController extends BaseController {

	@Autowired
	private AfterSaleService service;

	@Autowired
	private AfterSaleApplicationRepository repository;

	@Autowired
	private AfterSaleReturnAddressRepository addressRepository;

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private MerchantSettingRepository merchantSettingRepository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private CacheEvictService cacheEvictService;

	@Autowired
	private CacheClearer clearer;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<AfterSaleApplicationDTO>> list(@RequestBody QueryInfo<AfterSaleApplicationDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getList(user.getId(), queryInfo));
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<AfterSaleApplicationDTO> load(@PathVariable Long id) throws Exception {
		User user = getRelatedCurrentUser();
		AfterSaleApplicationDTO dto = service.load(user.getId(), id).asDTO();
		if (dto.getAgree() == null && dto.getReturnAddress() == null) {
			Optional<Merchant> merchant = merchantRepository.findById(user.getId());
			Optional<MerchantSetting> setting = merchantSettingRepository.findByMerchantAndCode(merchant.get(), MerchantSettingConstant.RETURN_ADDRESS);
			if (setting.isPresent()) {
				MerchantSetting data = setting.get();
				Map<String, String> returnAddress = mapper.readValue(data.getValue(), Map.class);
				ClientAddressDTO address = new ClientAddressDTO();
				address.setName(returnAddress.get("name"));
				address.setPhone(returnAddress.get("phone"));
				address.setLocation(returnAddress.get("location"));
				dto.setReturnAddress(address);
			}
		}
		return new ResultBean<>(dto);
	}

	@RequestMapping(path = "/audit")
	public ResultBean<Void> audit(@RequestBody AfterSaleApplicationDTO dto) {
		AssertUtil.assertNotNull(dto.getAgree(), "必须选择审核结果");
		User user = getRelatedCurrentUser();
		AfterSaleApplication data = service.load(user.getId(), dto.getId());
		AssertUtil.assertTrue(AfterSaleStatus.Processing.equals(data.getStatus()), "状态有误，不能审核该申请记录");
		AfterSaleStatus status = null;
		if (dto.getAgree()) {
			switch (data.getType()) {
			case Change:
			case SalesReturn: {
				status = AfterSaleStatus.WaitForReturn;
				AssertUtil.assertNotNull(dto.getReturnAddress(), "必须填写回寄地址");
				AfterSaleReturnAddress address = new AfterSaleReturnAddress();
				BeanUtils.copyProperties(dto.getReturnAddress(), address, "id");
				address.setApplication(data);
				addressRepository.save(address);
			}
				break;
			case RefundOnly:
				status = AfterSaleStatus.WaitForRefund;
				break;
			default:
				throw new BusinessException("暂不支持该类型的操作");
			}
		} else {
			AssertUtil.assertNotEmpty(dto.getRejectReason(), "拒绝原因不能为空");
			status = AfterSaleStatus.Rejected;
			data.setRejectReason(dto.getRejectReason());
			data.setRejectTime(new Date());
			cacheEvictService.clearClientAfterSaleUnFinishedtNums(data.getClient().getId());
			clearer.clearClientOrders(data.getOrder());
		}
		data.setStatus(status);
		data.setProcessedTime(new Date());
		repository.save(data);
		clearer.clearAfterSales(data);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/refund")
	public ResultBean<Void> refund(@RequestBody AfterSaleApplicationDTO dto) throws Exception {
		User user = getRelatedCurrentUser();
		service.refund(user.getId(), dto);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/receive/params/{id}")
	public ResultBean<Void> receive(@PathVariable Long id) throws Exception {
		User user = getRelatedCurrentUser();
		service.receive(user.getId(), id);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/sent")
	public ResultBean<Void> sent(@RequestBody AfterSaleApplicationDTO dto) throws Exception {
		User user = getRelatedCurrentUser();
		service.sent(user.getId(), dto);
		return new ResultBean<>();
	}

}
