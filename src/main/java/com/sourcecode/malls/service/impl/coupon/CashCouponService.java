package com.sourcecode.malls.service.impl.coupon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.coupon.cash.CashClientCoupon;
import com.sourcecode.malls.domain.coupon.cash.CashCouponConsumeEventSetting;
import com.sourcecode.malls.domain.coupon.cash.CashCouponInviteEventSetting;
import com.sourcecode.malls.domain.coupon.cash.CashCouponOrderLimitedSetting;
import com.sourcecode.malls.domain.coupon.cash.CashCouponSetting;
import com.sourcecode.malls.domain.goods.GoodsCategory;
import com.sourcecode.malls.domain.goods.GoodsItem;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.coupon.cash.CashClientCouponDTO;
import com.sourcecode.malls.dto.coupon.cash.CashCouponHxDTO;
import com.sourcecode.malls.dto.coupon.cash.CashCouponOrderLimitedSettingDTO;
import com.sourcecode.malls.dto.coupon.cash.CashCouponSettingDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.ClientCouponStatus;
import com.sourcecode.malls.enums.CouponSettingStatus;
import com.sourcecode.malls.exception.BusinessException;
import com.sourcecode.malls.repository.jpa.impl.coupon.CashClientCouponRepository;
import com.sourcecode.malls.repository.jpa.impl.coupon.CashCouponConsumeEventSettingRepository;
import com.sourcecode.malls.repository.jpa.impl.coupon.CashCouponInviteEventSettingRepository;
import com.sourcecode.malls.repository.jpa.impl.coupon.CashCouponOrderLimitedSettingRepository;
import com.sourcecode.malls.repository.jpa.impl.coupon.CashCouponSettingRepository;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsCategoryRepository;
import com.sourcecode.malls.repository.jpa.impl.goods.GoodsItemRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.util.AssertUtil;

@Service
@Transactional
public class CashCouponService {
	@Autowired
	protected CashCouponSettingRepository settingRepository;
	@Autowired
	protected CashCouponOrderLimitedSettingRepository limitedSettingRepository;
	@Autowired
	protected CashCouponConsumeEventSettingRepository consumeRepository;
	@Autowired
	protected CashCouponInviteEventSettingRepository inviteRepository;
	@Autowired
	protected CashClientCouponRepository clientRepository;
	@Autowired
	protected GoodsCategoryRepository categoryRepository;
	@Autowired
	protected GoodsItemRepository itemRepository;
	@Autowired
	protected MerchantRepository merchantRepository;

	@Transactional(readOnly = true)
	public PageResult<CashCouponSettingDTO> getSettingList(Long merchantId, QueryInfo<SimpleQueryDTO> queryInfo) {
		Page<CashCouponSetting> page = null;
		Specification<CashCouponSetting> spec = new Specification<CashCouponSetting>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<CashCouponSetting> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("merchant"), merchantId));
				predicate.add(criteriaBuilder.equal(root.get("enabled"), true));
				if (queryInfo.getData() != null) {
					if (!StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
						String like = "%" + queryInfo.getData().getSearchText() + "%";
						predicate.add(criteriaBuilder.like(root.get("name"), like));
					}
					if (queryInfo.getData().getStartTime() != null) {
						predicate.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"),
								queryInfo.getData().getStartTime()));
					}
					if (queryInfo.getData().getEndTime() != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(queryInfo.getData().getEndTime());
						c.add(Calendar.DATE, 1);
						predicate.add(criteriaBuilder.lessThan(root.get("startDate"), c.getTime()));
					}
					if (!StringUtils.isEmpty(queryInfo.getData().getStatusText())) {
						if (!"all".equalsIgnoreCase(queryInfo.getData().getStatusText())) {
							predicate.add(criteriaBuilder.equal(root.get("status"),
									CouponSettingStatus.valueOf(queryInfo.getData().getStatusText())));
						}
					}
					return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
				}
				return null;
			}
		};
		page = settingRepository.findAll(spec, queryInfo.getPage().pageable());
		return new PageResult<>(page.get().map(it -> it.asDTO(false)).collect(Collectors.toList()),
				page.getTotalElements());
	}

	public CashCouponSettingDTO get(Long merchantId, Long id) {
		Optional<CashCouponSetting> data = settingRepository.findById(id);
		AssertUtil.assertTrue(
				data.isPresent() && data.get().isEnabled() && data.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return data.get().asDTO(true);
	}

	@Transactional(readOnly = true)
	public PageResult<CashClientCouponDTO> getClientList(Long merchantId, QueryInfo<CashClientCouponDTO> queryInfo) {
		Page<CashClientCoupon> page = null;
		Specification<CashClientCoupon> spec = new Specification<CashClientCoupon>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<CashClientCoupon> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				if (queryInfo.getData() != null) {
					List<Predicate> predicate = new ArrayList<>();
					predicate.add(criteriaBuilder.equal(root.get("merchant"), merchantId));
					predicate.add(criteriaBuilder.equal(root.get("setting"), queryInfo.getData().getId()));
					if (!StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
						String like = "%" + queryInfo.getData().getSearchText() + "%";
						predicate.add(criteriaBuilder.or(criteriaBuilder.like(root.get("couponId"), like),
								criteriaBuilder.like(root.join("client").get("username"), like)));
					}
					if (queryInfo.getData().getStartTime() != null) {
						predicate.add(criteriaBuilder.greaterThanOrEqualTo(root.get("receivedTime"),
								queryInfo.getData().getStartTime()));
					}
					if (queryInfo.getData().getEndTime() != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(queryInfo.getData().getEndTime());
						c.add(Calendar.DATE, 1);
						predicate.add(criteriaBuilder.lessThan(root.get("receivedTime"), c.getTime()));
					}
					if (!StringUtils.isEmpty(queryInfo.getData().getStatusText())) {
						if (!"all".equalsIgnoreCase(queryInfo.getData().getStatusText())) {
							predicate.add(criteriaBuilder.equal(root.get("status"),
									ClientCouponStatus.valueOf(queryInfo.getData().getStatusText())));
						}
					}
					return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
				}
				return null;
			}
		};
		page = clientRepository.findAll(spec, queryInfo.getPage().pageable());
		return new PageResult<>(page.get().map(it -> it.asDTO()).collect(Collectors.toList()), page.getTotalElements());
	}

	public CashCouponSetting saveBaseInfo(Long merchantId, CashCouponSettingDTO dto) {
		CashCouponSetting data = null;
		if (dto.getId() != null && dto.getId() > 0) {
			Optional<CashCouponSetting> dataOp = settingRepository.findById(dto.getId());
			AssertUtil.assertTrue(
					dataOp.isPresent() && dataOp.get().isEnabled()
							&& dataOp.get().getMerchant().getId().equals(merchantId),
					ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
		} else {
			Optional<Merchant> merchant = merchantRepository.findById(merchantId);
			AssertUtil.assertTrue(merchant.isPresent(), "商家不存在");
			data = new CashCouponSetting();
			data.setStatus(CouponSettingStatus.WaitForPut);
			data.setMerchant(merchant.get());
			data.setEnabled(true);
		}
		if (CouponSettingStatus.WaitForPut.equals(data.getStatus())) {
			data.setDescription(dto.getDescription());
			data.setEndDate(dto.getEndDate());
			data.setImgPath(dto.getImgPath());
			data.setName(dto.getName());
			data.setStartDate(dto.getStartDate());
			data.setTotalNums(dto.getTotalNums());
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, -1);
			AssertUtil.assertTrue(data.getStartDate().after(c.getTime()), "生效时间必须是今天以后(包括今天)");
			if (data.getEndDate() != null) {
				AssertUtil.assertTrue(!data.getEndDate().before(data.getStartDate()), "过期时间必须是生效日期以后(包括生效日期当天)");
			}
		} else {
			data.setImgPath(dto.getImgPath());
			data.setDescription(dto.getDescription());
		}
		settingRepository.save(data);
		return data;
	}

	public void updateStatus(Long merchantId, Long id, Boolean status) {
		Optional<CashCouponSetting> dataOp = settingRepository.findById(id);
		AssertUtil.assertTrue(
				dataOp.isPresent() && dataOp.get().isEnabled() && dataOp.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		CashCouponSetting data = dataOp.get();
		AssertUtil.assertNotNull(data.getEventType(), "请先编辑赠送条件");
		AssertUtil.assertNotNull(data.getHxType(), "请先编辑核销条件");
		if (status) {
			AssertUtil.assertTrue(!CouponSettingStatus.PutAway.equals(data.getStatus()), "已经上架过");
			AssertUtil.assertTrue(CouponSettingStatus.WaitForPut.equals(data.getStatus())
					|| CouponSettingStatus.SoldOut.equals(data.getStatus()), "状态有误，上架失败");
			data.setStatus(CouponSettingStatus.PutAway);
		} else {
			AssertUtil.assertTrue(CouponSettingStatus.PutAway.equals(data.getStatus()), "状态有误，下架失败");
			data.setStatus(CouponSettingStatus.SoldOut);
		}
		settingRepository.save(data);
	}

	@SuppressWarnings("incomplete-switch")
	public void saveZsCondition(Long merchantId, CashCouponSettingDTO dto) {
		AssertUtil.assertNotNull(dto.getId(), ExceptionMessageConstant.NO_SUCH_RECORD);
		Optional<CashCouponSetting> dataOp = settingRepository.findById(dto.getId());
		AssertUtil.assertTrue(
				dataOp.isPresent() && dataOp.get().isEnabled() && dataOp.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		CashCouponSetting data = dataOp.get();
		AssertUtil.assertTrue(CouponSettingStatus.WaitForPut.equals(data.getStatus()), "已经上架过，不能修改");
		AssertUtil.assertNotNull(dto.getEventType(), "必须选择一种用户行为");
		switch (dto.getEventType()) {
		case Consume: {
			AssertUtil.assertNotNull(dto.getConsumeSetting(), "请编辑赠送条件");
			AssertUtil.assertNotNull(dto.getConsumeSetting().getType(), "必须选择关联属性");
			if (data.getInviteSetting() != null) {
				inviteRepository.delete(data.getInviteSetting());
			}
			CashCouponConsumeEventSetting setting = data.getConsumeSetting();
			if (setting == null) {
				setting = new CashCouponConsumeEventSetting();
				setting.setSetting(data);
			}
			BeanUtils.copyProperties(dto.getConsumeSetting(), setting, "id", "categories", "items");
			switch (dto.getConsumeSetting().getType()) {
			case Category: {
				AssertUtil.assertTrue(!CollectionUtils.isEmpty(dto.getConsumeSetting().getCategoryIds()), "必须关联分类");
				List<GoodsCategory> list = new ArrayList<>();
				List<GoodsCategory> displayList = new ArrayList<>();
				for (Long id : dto.getConsumeSetting().getCategoryIds()) {
					Optional<GoodsCategory> categoryOp = categoryRepository.findById(id);
					if (categoryOp.isPresent()) {
						GoodsCategory category = categoryOp.get();
						if (category.getMerchant().getId().equals(merchantId)) {
							putCategories(list, category);
							displayList.add(category);
						}
					}
				}
				AssertUtil.assertTrue(!CollectionUtils.isEmpty(list), "必须关联分类或商品");
				AssertUtil.assertTrue(!CollectionUtils.isEmpty(displayList), "必须关联分类或商品");
				setting.setCategories(displayList);
				setting.setRealCategories(list);
				setting.setItems(null);
			}
				break;
			case Item: {
				AssertUtil.assertTrue(!CollectionUtils.isEmpty(dto.getConsumeSetting().getItemIds()), "必须关联商品");
				List<GoodsItem> list = new ArrayList<>();
				for (Long id : dto.getConsumeSetting().getItemIds()) {
					Optional<GoodsItem> itemOp = itemRepository.findById(id);
					if (itemOp.isPresent() && itemOp.get().getMerchant().getId().equals(merchantId)) {
						list.add(itemOp.get());
					}
				}
				AssertUtil.assertTrue(!CollectionUtils.isEmpty(list), "必须关联分类或商品");
				setting.setItems(list);
				setting.setCategories(null);
				setting.setRealCategories(null);
			}
				break;
			}
			consumeRepository.save(setting);
		}
			break;
		case Invite: {
			AssertUtil.assertNotNull(dto.getInviteSetting(), "请编辑赠送条件");
			if (data.getConsumeSetting() != null) {
				consumeRepository.delete(data.getConsumeSetting());
			}
			CashCouponInviteEventSetting setting = data.getInviteSetting();
			if (setting == null) {
				setting = new CashCouponInviteEventSetting();
				setting.setSetting(data);
			}
			BeanUtils.copyProperties(dto.getInviteSetting(), setting, "id");
			inviteRepository.save(setting);
		}
			break;
		default:
		}
		data.setEventType(dto.getEventType());
		settingRepository.save(data);
	}

	@SuppressWarnings("incomplete-switch")
	public void saveHxCondition(Long merchantId, CashCouponHxDTO dto) {
		AssertUtil.assertNotNull(dto.getId(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertNotNull(dto.getType(), "必须选择关联属性");
		Optional<CashCouponSetting> dataOp = settingRepository.findById(dto.getId());
		AssertUtil.assertTrue(
				dataOp.isPresent() && dataOp.get().isEnabled() && dataOp.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		CashCouponSetting data = dataOp.get();
		AssertUtil.assertTrue(CouponSettingStatus.WaitForPut.equals(data.getStatus()), "已经上架过，不能修改");
		data.setLimitedNums(dto.getLimitedNums());
		data.setHxType(dto.getType());
		switch (dto.getType()) {
		case Category: {
			AssertUtil.assertTrue(!CollectionUtils.isEmpty(dto.getCategoryIds()), "必须关联分类");
			List<GoodsCategory> list = new ArrayList<>();
			List<GoodsCategory> displayList = new ArrayList<>();
			for (Long id : dto.getCategoryIds()) {
				Optional<GoodsCategory> categoryOp = categoryRepository.findById(id);
				if (categoryOp.isPresent()) {
					GoodsCategory category = categoryOp.get();
					if (category.getMerchant().getId().equals(merchantId)) {
						putCategories(list, category);
						displayList.add(category);
					}
				}
			}
			AssertUtil.assertTrue(!CollectionUtils.isEmpty(list), "必须关联分类或商品");
			AssertUtil.assertTrue(!CollectionUtils.isEmpty(displayList), "必须关联分类或商品");
			data.setCategories(displayList);
			data.setRealCategories(list);
			data.setItems(null);
		}
			break;
		case Item: {
			AssertUtil.assertTrue(!CollectionUtils.isEmpty(dto.getItemIds()), "必须关联商品");
			List<GoodsItem> list = new ArrayList<>();
			for (Long id : dto.getItemIds()) {
				Optional<GoodsItem> itemOp = itemRepository.findById(id);
				if (itemOp.isPresent() && itemOp.get().getMerchant().getId().equals(merchantId)) {
					list.add(itemOp.get());
				}
			}
			AssertUtil.assertTrue(!CollectionUtils.isEmpty(list), "必须关联分类或商品");
			data.setItems(list);
			data.setCategories(null);
			data.setRealCategories(null);
		}
			break;
		}
		settingRepository.save(data);
	}

	private void putCategories(List<GoodsCategory> list, GoodsCategory category) {
		if (!CollectionUtils.isEmpty(category.getSubList())) {
			for (GoodsCategory sub : category.getSubList()) {
				putCategories(list, sub);
			}
		} else {
			boolean found = false;
			for (GoodsCategory data : list) {
				if (data.getId().equals(category.getId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				list.add(category);
			}
		}
	}

	public void delete(Long merchantId, List<Long> ids) {
		for (Long id : ids) {
			Optional<CashCouponSetting> dataOp = settingRepository.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(merchantId)) {
				switch (dataOp.get().getStatus()) {
				case WaitForPut:
					settingRepository.delete(dataOp.get());
					break;
				case SentOut:
				case SoldOut: {
					dataOp.get().setEnabled(false);
					settingRepository.save(dataOp.get());
				}
					break;
				default:
					throw new BusinessException("不能删除记录[" + dataOp.get().getName() + "]");
				}

			}
		}
	}

	@Transactional(readOnly = true)
	public PageResult<CashCouponOrderLimitedSettingDTO> getOrderLimitedSettingList(Long merchantId,
			QueryInfo<Void> queryInfo) {
		Optional<Merchant> merchant = merchantRepository.findById(merchantId);
		AssertUtil.assertTrue(merchant.isPresent(), "商家不存在");
		Page<CashCouponOrderLimitedSetting> result = limitedSettingRepository.findAllByMerchant(merchant.get(),
				queryInfo.getPage().pageable());
		return new PageResult<>(result.get().map(it -> it.asDTO()).collect(Collectors.toList()),
				result.getTotalElements());
	}

	@Transactional(readOnly = true)
	public CashCouponOrderLimitedSettingDTO getOrderLimitedSetting(Long id, Long merchantId) {
		Optional<CashCouponOrderLimitedSetting> data = limitedSettingRepository.findById(id);
		AssertUtil.assertTrue(data.isPresent() && data.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return data.get().asDTO();
	}

	public void saveOrderLimitedSetting(Long merchantId, CashCouponOrderLimitedSettingDTO dto) {
		Optional<Merchant> merchant = merchantRepository.findById(merchantId);
		AssertUtil.assertTrue(merchant.isPresent(), "商家不存在");
		CashCouponOrderLimitedSetting data = null;
		if (dto.getId() != null) {
			Optional<CashCouponOrderLimitedSetting> dataOp = limitedSettingRepository.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(merchantId),
					ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
		} else {
			data = new CashCouponOrderLimitedSetting();
			data.setMerchant(merchant.get());
		}
		BeanUtils.copyProperties(dto, data);
		limitedSettingRepository.save(data);
	}

	public void deleteOrderLimitedSetting(Long merchantId, List<Long> ids) {
		Optional<Merchant> merchant = merchantRepository.findById(merchantId);
		AssertUtil.assertTrue(merchant.isPresent(), "商家不存在");
		for (Long id : ids) {
			Optional<CashCouponOrderLimitedSetting> data = limitedSettingRepository.findById(id);
			if (data.isPresent() && data.get().getMerchant().getId().equals(merchantId)) {
				limitedSettingRepository.delete(data.get());
			}
		}
	}
}
