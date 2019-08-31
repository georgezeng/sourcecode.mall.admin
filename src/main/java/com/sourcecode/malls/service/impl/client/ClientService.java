package com.sourcecode.malls.service.impl.client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.sourcecode.malls.constants.ExceptionMessageConstant;
import com.sourcecode.malls.domain.client.Client;
import com.sourcecode.malls.domain.client.ClientActivityEvent;
import com.sourcecode.malls.domain.client.ClientLevelSetting;
import com.sourcecode.malls.domain.client.ClientPoints;
import com.sourcecode.malls.domain.client.ClientPointsJournal;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.client.ClientActivityEventDTO;
import com.sourcecode.malls.dto.client.ClientDTO;
import com.sourcecode.malls.dto.client.ClientLevelSettingDTO;
import com.sourcecode.malls.dto.client.ClientPointsJournalDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.BalanceType;
import com.sourcecode.malls.enums.ClientPointsType;
import com.sourcecode.malls.exception.BusinessException;
import com.sourcecode.malls.repository.jpa.impl.client.ClientActivityEventRepository;
import com.sourcecode.malls.repository.jpa.impl.client.ClientLevelSettingRepository;
import com.sourcecode.malls.repository.jpa.impl.client.ClientRepository;
import com.sourcecode.malls.repository.jpa.impl.coupon.ClientPointsJournalRepository;
import com.sourcecode.malls.repository.jpa.impl.coupon.ClientPointsRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.base.JpaService;
import com.sourcecode.malls.service.impl.CacheEvictService;
import com.sourcecode.malls.util.AssertUtil;

@Service
@Transactional
public class ClientService implements JpaService<Client, Long> {

	@Autowired
	private ClientRepository repository;

	@Autowired
	private ClientPointsRepository pointsRepository;

	@Autowired
	private ClientPointsJournalRepository journalRepository;

	@Autowired
	private ClientLevelSettingRepository settingRepository;

	@Autowired
	private ClientActivityEventRepository activityRepository;

	@Autowired
	private MerchantRepository merchantRepository;
	
	@Autowired
	private CacheEvictService cacheEvictService;

	@Autowired
	private EntityManager em;

	@Override
	public JpaRepository<Client, Long> getRepository() {
		return repository;
	}

	@Transactional(readOnly = true)
	public Page<Client> findAll(QueryInfo<ClientDTO> queryInfo) {
		Page<Client> pageResult = null;
		Specification<Client> spec = new Specification<Client>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				ClientDTO data = queryInfo.getData();
				if (data != null) {
					predicate.add(criteriaBuilder.equal(root.get("merchant"), data.getMerchantId()));
					if (data.getId() != null) {
						predicate.add(criteriaBuilder.equal(root.get("parent"), data.getId()));
					}
					String searchText = queryInfo.getData().getSearchText();
					if (!StringUtils.isEmpty(searchText)) {
						String like = "%" + searchText + "%";
						predicate.add(
								criteriaBuilder.or(criteriaBuilder.like(root.get("username").as(String.class), like),
										criteriaBuilder.like(root.get("nickname").as(String.class), like)));
					}
					if (!"all".equals(data.getStatusText())) {
						predicate
								.add(criteriaBuilder.equal(root.get("enabled"), Boolean.valueOf(data.getStatusText())));
					}
					if (queryInfo.getData().getStartTime() != null) {
						predicate.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"),
								queryInfo.getData().getStartTime()));
					}
					if (queryInfo.getData().getEndTime() != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(queryInfo.getData().getEndTime());
						c.add(Calendar.DATE, 1);
						predicate.add(criteriaBuilder.lessThan(root.get("createTime"), c.getTime()));
					}
					return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
				} else {
					return null;
				}
			}
		};
		pageResult = repository.findAll(spec, queryInfo.getPage().pageable());
		return pageResult;
	}

	@Transactional(readOnly = true)
	public Page<ClientPoints> findAllPoints(Long merchantId, QueryInfo<SimpleQueryDTO> queryInfo) {
		Page<ClientPoints> pageResult = null;
		Specification<ClientPoints> spec = new Specification<ClientPoints>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<ClientPoints> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				Join<ClientPoints, Client> join = root.join("client");
				predicate.add(criteriaBuilder.equal(join.get("merchant"), merchantId));
				if (queryInfo.getData() != null) {
					if (!StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
						String like = "%" + queryInfo.getData().getSearchText() + "%";
						predicate.add(criteriaBuilder.like(join.get("username"), like));
					}
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		pageResult = pointsRepository.findAll(spec, queryInfo.getPage().pageable());
		return pageResult;
	}

	@Transactional(readOnly = true)
	public Page<ClientPointsJournal> findAllPointsJournal(Long merchantId, QueryInfo<SimpleQueryDTO> queryInfo) {
		AssertUtil.assertNotNull(queryInfo.getData(), "查询条件有误");
		Optional<Client> client = repository.findById(queryInfo.getData().getId());
		AssertUtil.assertTrue(client.isPresent() && client.get().getMerchant().getId().equals(merchantId), "用户不存在");
		Page<ClientPointsJournal> pageResult = null;
		Specification<ClientPointsJournal> spec = new Specification<ClientPointsJournal>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<ClientPointsJournal> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("client"), client.get()));
				if (queryInfo.getData() != null) {
					if (!StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
						String like = "%" + queryInfo.getData().getSearchText() + "%";
						predicate.add(criteriaBuilder.like(root.get("orderId"), like));
					}
					if (queryInfo.getData().getStartTime() != null) {
						predicate.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"),
								queryInfo.getData().getStartTime()));
					}
					if (queryInfo.getData().getEndTime() != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(queryInfo.getData().getEndTime());
						c.add(Calendar.DATE, 1);
						predicate.add(criteriaBuilder.lessThan(root.get("createTime"), c.getTime()));
					}
					if (!StringUtils.isEmpty(queryInfo.getData().getStatusText())) {
						if (!"all".equalsIgnoreCase(queryInfo.getData().getStatusText())) {
							predicate.add(criteriaBuilder.equal(root.get("balanceType"),
									BalanceType.valueOf(queryInfo.getData().getStatusText())));
						}
					}
					return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		pageResult = journalRepository.findAll(spec, queryInfo.getPage().pageable());
		return pageResult;
	}

	public void createPointsJournal(Long merchantId, ClientPointsJournalDTO dto) {
		Optional<Client> client = repository.findById(dto.getClientId());
		AssertUtil.assertTrue(client.isPresent() && client.get().getMerchant().getId().equals(merchantId), "用户不存在");
		ClientPointsJournal journal = new ClientPointsJournal();
		BeanUtils.copyProperties(dto, journal);
		journal.setClient(client.get());
		ClientPoints points = client.get().getPoints();
		em.lock(points, LockModeType.PESSIMISTIC_WRITE);
		if (BalanceType.In.equals(dto.getBalanceType())) {
			journal.setType(ClientPointsType.ManuallyAdded);
			points.setCurrentAmount(points.getCurrentAmount().add(dto.getBonusAmount()));
			points.setAccInAmount(points.getAccInAmount().add(dto.getBonusAmount()));
		} else {
			journal.setType(ClientPointsType.ManuallyReduce);
			points.setCurrentAmount(points.getCurrentAmount().subtract(dto.getBonusAmount()));
			points.setAccOutAmount(points.getAccOutAmount().add(dto.getBonusAmount()));
		}
		pointsRepository.save(points);
		journalRepository.save(journal);
	}

	public PageResult<ClientLevelSettingDTO> findAllLevelSetting(Long merchantId) {
		Optional<Merchant> merchant = merchantRepository.findById(merchantId);
		AssertUtil.assertTrue(merchant.isPresent(), "商家不存在");
		List<ClientLevelSettingDTO> list = settingRepository.findAllByMerchantOrderByLevelAsc(merchant.get()).stream()
				.map(it -> it.asDTO()).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(list)) {
			for (int i = list.size() - 1; i > -1; i--) {
				ClientLevelSettingDTO dto = list.get(i);
				if (StringUtils.isEmpty(dto.getName())) {
					continue;
				}
				if (dto.getLevel() > 0) {
					dto.setTop(true);
					break;
				}
			}
		}
		return new PageResult<>(list, list.size());
	}

	public ClientLevelSettingDTO loadLevelSetting(Long merchantId, Long id) {
		Optional<ClientLevelSetting> dataOp = settingRepository.findById(id);
		if (dataOp.isPresent()) {
			AssertUtil.assertTrue(dataOp.get().getMerchant().getId().equals(merchantId),
					ExceptionMessageConstant.NO_SUCH_RECORD);
			;
			return dataOp.get().asDTO();
		}
		return null;
	}

	public void save(Long merchantId, ClientLevelSettingDTO dto) {
		ClientLevelSetting data = null;
		if (dto.getId() == null) {
			data = new ClientLevelSetting();
			Optional<Merchant> merchant = merchantRepository.findById(merchantId);
			AssertUtil.assertTrue(merchant.isPresent(), "商家不存在");
			data.setMerchant(merchant.get());
			Optional<ClientLevelSetting> sameLevel = settingRepository.findByMerchantAndLevel(merchant.get(), dto.getLevel());
			AssertUtil.assertTrue(!sameLevel.isPresent(), "已经存在该级别，请重新定义级别");
		} else {
			data = settingRepository.findById(dto.getId()).orElse(null);
			AssertUtil.assertNotNull(data, ExceptionMessageConstant.NO_SUCH_RECORD);
		}
		AssertUtil.assertNotEmpty(dto.getName(), "等级名称不能为空");
		AssertUtil.assertNotNull(dto.getUpToAmount(), "消费累计不能为空");
		AssertUtil.assertNotNull(dto.getDiscount(), "折扣不能为空");
		AssertUtil.assertNotNull(dto.getDiscountInActivity(), "活动日折扣不能为空");
		BeanUtils.copyProperties(dto, data, "id", "merchant");
		settingRepository.save(data);
	}

	public void clearLevelSetting(Long merchantId, Long id) {
		Optional<ClientLevelSetting> settingOp = settingRepository.findById(id);
		AssertUtil.assertTrue(settingOp.isPresent() && settingOp.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		ClientLevelSetting setting = settingOp.get();
		Optional<ClientLevelSetting> topSetting = settingRepository
				.findFirstByMerchantAndNameNotNullOrderByLevelDesc(setting.getMerchant());
		AssertUtil.assertTrue(topSetting.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
		AssertUtil.assertTrue(topSetting.get().getId().equals(setting.getId()), "只能清除最高级");
		AssertUtil.assertTrue(setting.getLevel() > 0, "不能清除最低级");
		setting.setDiscount(null);
		setting.setDiscountInActivity(null);
		setting.setName(null);
		setting.setUpToAmount(null);
		settingRepository.save(setting);
	}

	public PageResult<ClientActivityEventDTO> findAllActivityEvents(Long merchantId, QueryInfo<SimpleQueryDTO> queryInfo) {
		Page<ClientActivityEvent> pageResult = null;
		Specification<ClientActivityEvent> spec = new Specification<ClientActivityEvent>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<ClientActivityEvent> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("merchant"), merchantId));
				if (queryInfo.getData() != null) {
					if (!StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
						String like = "%" + queryInfo.getData().getSearchText() + "%";
						predicate.add(criteriaBuilder.like(root.get("name"), like));
					}
					if (queryInfo.getData().getStartTime() != null) {
						predicate.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"),
								queryInfo.getData().getStartTime()));
					}
					if (queryInfo.getData().getEndTime() != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(queryInfo.getData().getEndTime());
						c.add(Calendar.DATE, 1);
						predicate.add(criteriaBuilder.lessThan(root.get("createTime"), c.getTime()));
					}
					if (!StringUtils.isEmpty(queryInfo.getData().getStatusText())) {
						Date now = new Date();
						switch (queryInfo.getData().getStatusText()) {
						case "paused": {
							predicate.add(criteriaBuilder.equal(root.get("paused"), true));
						}
							break;
						case "in": {
							predicate.add(
									criteriaBuilder.or(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), now),
											criteriaBuilder.lessThanOrEqualTo(root.get("endTime"), now)));
						}
							break;
						case "unstarted": {
							predicate.add(criteriaBuilder.lessThan(root.get("startTime"), now));
						}
							break;
						case "stopped": {
							predicate.add(criteriaBuilder.greaterThan(root.get("endTime"), now));
						}
							break;
						}
					}
					return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		pageResult = activityRepository.findAll(spec, queryInfo.getPage().pageable());
		return new PageResult<>(pageResult.get().map(it -> it.asDTO()).collect(Collectors.toList()),
				pageResult.getTotalElements());
	}

	public ClientActivityEventDTO loadActivityEvent(Long merchantId, Long id) {
		Optional<ClientActivityEvent> data = activityRepository.findById(id);
		AssertUtil.assertTrue(
				data.isPresent() && !data.get().isDeleted() && data.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return data.get().asDTO();
	}

	public void deleteActivityEvents(Long merchantId, List<Long> ids) {
		if (!CollectionUtils.isEmpty(ids)) {
			Date now = new Date();
			for (Long id : ids) {
				Optional<ClientActivityEvent> dataOp = activityRepository.findById(id);
				if (dataOp.isPresent() && !dataOp.get().isDeleted()
						&& dataOp.get().getMerchant().getId().equals(merchantId)) {
					ClientActivityEvent data = dataOp.get();
					if (data.getStartTime().after(now)) {
						activityRepository.delete(data);
					} else if (!now.after(data.getEndTime())) {
						data.setDeleted(true);
						activityRepository.save(data);
					} else {
						throw new BusinessException("活动进行中，不能删除");
					}
				}
			}
			cacheEvictService.clearClientActivityEventTime(merchantId);
		}
	}

	public void triggerPauseActivityEvent(Long merchantId, Long id, boolean paused) {
		Optional<ClientActivityEvent> dataOp = activityRepository.findById(id);
		AssertUtil.assertTrue(
				dataOp.isPresent() && !dataOp.get().isDeleted()
						&& dataOp.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		ClientActivityEvent data = dataOp.get();
		Date now = new Date();
		AssertUtil.assertTrue(!data.getStartTime().after(now), "活动还未开始");
		AssertUtil.assertTrue(!now.after(data.getEndTime()), "活动已经结束");
		data.setPaused(paused);
		activityRepository.save(data);
		cacheEvictService.clearClientActivityEventTime(merchantId);
	}

	public void save(Long merchantId, ClientActivityEventDTO dto) {
		Date now = new Date();
		AssertUtil.assertTrue(dto.getStartTime() != null && dto.getStartTime().after(now), "开始时间必须大于当前时间");
		AssertUtil.assertTrue(dto.getEndTime() != null && dto.getEndTime().after(now), "结束时间必须大于当前时间");
		AssertUtil.assertTrue(
				dto.getStartTime() != null && dto.getEndTime() != null && dto.getEndTime().after(dto.getStartTime()),
				"结束时间必须大于开始时间");
		ClientActivityEvent data = null;
		if (dto.getId() != null) {
			Optional<ClientActivityEvent> dataOp = activityRepository.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
			if (data.getStartTime().before(now) && !now.after(data.getEndTime())) {
				AssertUtil.assertTrue(data.isPaused(), "请先中止活动才能编辑");
			} else if (data.getEndTime().before(now)) {
				throw new BusinessException("活动已结束，不能编辑");
			}
		} else {
			Optional<Merchant> merchant = merchantRepository.findById(merchantId);
			AssertUtil.assertTrue(merchant.isPresent(), "商家不存在");
			data = dto.asEntity();
			data.setMerchant(merchant.get());
			data.setPaused(false);
			data.setDeleted(false);
		}
		activityRepository.save(data);
		cacheEvictService.clearClientActivityEventTime(merchantId);
	}

}
