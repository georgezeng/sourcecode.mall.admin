package com.sourcecode.malls.service.impl.client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sourcecode.malls.domain.client.Client;
import com.sourcecode.malls.dto.client.ClientDTO;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.client.ClientRepository;
import com.sourcecode.malls.service.base.JpaService;

@Service
@Transactional
public class ClientService implements JpaService<Client, Long> {

	@Autowired
	private ClientRepository repository;

	@Override
	public JpaRepository<Client, Long> getRepository() {
		return repository;
	}

	@SuppressWarnings("unchecked")
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
		pageResult = ((JpaSpecificationExecutor<Client>) getRepository()).findAll(spec, queryInfo.getPage().pageable());
		return pageResult;
	}

}
