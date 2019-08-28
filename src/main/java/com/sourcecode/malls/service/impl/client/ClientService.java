package com.sourcecode.malls.service.impl.client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

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
import org.springframework.util.StringUtils;

import com.sourcecode.malls.domain.client.Client;
import com.sourcecode.malls.domain.client.ClientPoints;
import com.sourcecode.malls.domain.client.ClientPointsJournal;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.client.ClientDTO;
import com.sourcecode.malls.dto.client.ClientPointsJournalDTO;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.BalanceType;
import com.sourcecode.malls.enums.ClientPointsType;
import com.sourcecode.malls.repository.jpa.impl.client.ClientRepository;
import com.sourcecode.malls.repository.jpa.impl.coupon.ClientPointsJournalRepository;
import com.sourcecode.malls.repository.jpa.impl.coupon.ClientPointsRepository;
import com.sourcecode.malls.service.base.JpaService;
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

}
