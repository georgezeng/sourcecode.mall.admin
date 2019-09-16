package com.sourcecode.malls.service.impl.merchant;

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
import org.springframework.util.StringUtils;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.merchant.AdvertisementSetting;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.merchant.AdvertisementSettingDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.AdvertisementType;
import com.sourcecode.malls.repository.jpa.impl.merchant.AdvertisementSettingRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.impl.CacheEvictService;
import com.sourcecode.malls.util.AssertUtil;

@Service
@Transactional
public class AdvertisementService {
	@Autowired
	private AdvertisementSettingRepository repository;
	@Autowired
	protected MerchantRepository merchantRepository;
	@Autowired
	protected CacheEvictService cacheEvictService;

	@Transactional(readOnly = true)
	public PageResult<AdvertisementSettingDTO> getList(Long merchantId, QueryInfo<SimpleQueryDTO> queryInfo) {
		Page<AdvertisementSetting> page = null;
		Specification<AdvertisementSetting> spec = new Specification<AdvertisementSetting>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<AdvertisementSetting> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("merchant"), merchantId));
				if (queryInfo.getData() != null) {
					if (!StringUtils.isEmpty(queryInfo.getData().getStatusText())) {
						if (!"all".equalsIgnoreCase(queryInfo.getData().getStatusText())) {
							predicate.add(criteriaBuilder.equal(root.get("type"), AdvertisementType.valueOf(queryInfo.getData().getStatusText())));
						}
					}
					if (!StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
						String like = "%" + queryInfo.getData().getSearchText() + "%";
						predicate.add(criteriaBuilder.like(root.get("name"), like));
					}
					if (queryInfo.getData().getStartTime() != null) {
						predicate.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), queryInfo.getData().getStartTime()));
					}
					if (queryInfo.getData().getEndTime() != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(queryInfo.getData().getEndTime());
						c.add(Calendar.DATE, 1);
						predicate.add(criteriaBuilder.lessThan(root.get("startDate"), c.getTime()));
					}
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		page = repository.findAll(spec, queryInfo.getPage().pageable());
		return new PageResult<>(page.get().map(it -> it.asDTO()).collect(Collectors.toList()), page.getTotalElements());
	}

	@Transactional(readOnly = true)
	public AdvertisementSettingDTO get(Long merchantId, Long id) {
		Optional<AdvertisementSetting> data = repository.findById(id);
		AssertUtil.assertTrue(data.isPresent() && data.get().getMerchant().getId().equals(merchantId), ExceptionMessageConstant.NO_SUCH_RECORD);
		return data.get().asDTO();
	}

	public void delete(Long merchantId, List<Long> ids) {
		for (Long id : ids) {
			Optional<AdvertisementSetting> dataOp = repository.findById(id);
			if (dataOp.isPresent() && dataOp.get().getMerchant().getId().equals(merchantId)) {
				repository.delete(dataOp.get());
				cacheEvictService.clearAdvertisementList(merchantId, dataOp.get().getType());
			}
		}
	}

	public AdvertisementSetting save(Long merchantId, AdvertisementSettingDTO dto) {
		Optional<Merchant> merchant = merchantRepository.findById(merchantId);
		AssertUtil.assertTrue(merchant.isPresent(), "商家不存在");
		AdvertisementSetting data = null;
		if (dto.getId() == null) {
			data = dto.asEntity();
			data.setMerchant(merchant.get());
		} else {
			Optional<AdvertisementSetting> dataOp = repository.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
			BeanUtils.copyProperties(dto, data, "id");
		}
		AssertUtil.assertTrue(data.getStartTime() != null && data.getEndTime() != null && !data.getStartTime().after(data.getEndTime()),
				"开始时间必须小于等于结束时间");
		repository.save(data);
		cacheEvictService.clearAdvertisementList(merchantId, data.getType());
		return data;
	}

}
