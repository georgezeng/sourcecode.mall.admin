package com.sourcecode.malls.service.impl.client;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
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
import com.sourcecode.malls.domain.client.ClientIdentity;
import com.sourcecode.malls.dto.client.ClientIdentityDTO;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.VerificationStatus;
import com.sourcecode.malls.repository.jpa.impl.client.ClientIdentityRepository;
import com.sourcecode.malls.service.base.JpaService;

@Service
@Transactional
public class ClientIdentityService implements JpaService<ClientIdentity, Long> {

	@Autowired
	private ClientIdentityRepository repository;

	@Override
	public JpaRepository<ClientIdentity, Long> getRepository() {
		return repository;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public Page<ClientIdentity> findAll(QueryInfo<ClientIdentityDTO> queryInfo) {
		Page<ClientIdentity> pageResult = null;
		Specification<ClientIdentity> spec = new Specification<ClientIdentity>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<ClientIdentity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				ClientIdentityDTO data = queryInfo.getData();
				if (data != null) {
					Join<ClientIdentity, Client> join = root.join("client");
					predicate.add(criteriaBuilder.equal(join.get("merchant"), data.getMerchantId()));
					String searchText = queryInfo.getData().getSearchText();
					if (!StringUtils.isEmpty(searchText)) {
						String like = "%" + searchText + "%";
						predicate.add(criteriaBuilder.or(criteriaBuilder.like(root.get("name").as(String.class), like),
								criteriaBuilder.like(root.get("number").as(String.class), like),
								criteriaBuilder.like(join.get("username").as(String.class), like)));
					}
					if (!"all".equals(data.getStatusText())) {
						predicate.add(criteriaBuilder.equal(root.get("status"), VerificationStatus.valueOf(data.getStatusText())));
					}
					return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
				} else {
					return null;
				}
			}
		};
		pageResult = ((JpaSpecificationExecutor<ClientIdentity>) getRepository()).findAll(spec, queryInfo.getPage().pageable());
		return pageResult;
	}

}
