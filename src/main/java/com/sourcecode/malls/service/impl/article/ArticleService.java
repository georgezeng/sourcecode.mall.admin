package com.sourcecode.malls.service.impl.article;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
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
import com.sourcecode.malls.domain.article.Article;
import com.sourcecode.malls.domain.article.ArticleCategory;
import com.sourcecode.malls.domain.merchant.Merchant;
import com.sourcecode.malls.dto.article.ArticleCategoryDTO;
import com.sourcecode.malls.dto.article.ArticleDTO;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.enums.ArticleCategoryType;
import com.sourcecode.malls.repository.jpa.impl.article.ArticleCategoryRepository;
import com.sourcecode.malls.repository.jpa.impl.article.ArticleRepository;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantRepository;
import com.sourcecode.malls.service.base.BaseService;
import com.sourcecode.malls.service.impl.CacheClearer;
import com.sourcecode.malls.util.AssertUtil;

@Service
@Transactional
public class ArticleService implements BaseService {

	@Autowired
	private ArticleCategoryRepository categoryRepository;

	@Autowired
	private ArticleRepository repository;

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private CacheClearer clearer;

	private String fileDir = "article";

	@Transactional(readOnly = true)
	public List<ArticleCategoryDTO> getAllCategory(Long merchantId) {
		Optional<Merchant> merchant = merchantRepository.findById(merchantId);
		AssertUtil.assertTrue(merchant.isPresent(), "商家不存在");
		return categoryRepository.findAllByMerchant(merchant.get()).stream().map(it -> it.asDTO())
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public PageResult<ArticleCategoryDTO> getCategoryList(Long merchantId, QueryInfo<SimpleQueryDTO> queryInfo) {
		Specification<ArticleCategory> spec = new Specification<ArticleCategory>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<ArticleCategory> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("merchant"), merchantId));
				if (queryInfo.getData() != null) {
					if (!StringUtils.isEmpty(queryInfo.getData().getStatusText())
							&& !"all".equalsIgnoreCase(queryInfo.getData().getStatusText())) {
						predicate.add(criteriaBuilder.equal(root.get("type"),
								ArticleCategoryType.valueOf(queryInfo.getData().getStatusText())));
					}
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
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		Page<ArticleCategory> result = categoryRepository.findAll(spec, queryInfo.getPage().pageable());
		return new PageResult<>(result.get().map(it -> it.asDTO()).collect(Collectors.toList()),
				result.getTotalElements());
	}

	@Transactional(readOnly = true)
	public PageResult<ArticleDTO> getList(Long merchantId, QueryInfo<SimpleQueryDTO> queryInfo) {
		Specification<Article> spec = new Specification<Article>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(criteriaBuilder.equal(root.get("merchant"), merchantId));
				if (queryInfo.getData() != null) {
					Join<Article, ArticleCategory> join = root.join("category");
					if (!StringUtils.isEmpty(queryInfo.getData().getStatusText())
							&& !"all".equalsIgnoreCase(queryInfo.getData().getStatusText())) {
						predicate.add(criteriaBuilder.equal(join.get("type"),
								ArticleCategoryType.valueOf(queryInfo.getData().getStatusText())));
					}
					if (!StringUtils.isEmpty(queryInfo.getData().getSearchText())) {
						String like = "%" + queryInfo.getData().getSearchText() + "%";
						predicate.add(criteriaBuilder.or(criteriaBuilder.like(join.get("name"), like),
								criteriaBuilder.like(root.get("title"), like)));
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
				}
				return query.where(predicate.toArray(new Predicate[] {})).getRestriction();
			}
		};
		Page<Article> result = repository.findAll(spec, queryInfo.getPage().pageable());
		return new PageResult<>(result.get().map(it -> it.asDTO()).collect(Collectors.toList()),
				result.getTotalElements());
	}

	@Transactional(readOnly = true)
	public ArticleCategory loadCategory(Long merchantId, Long id) {
		Optional<ArticleCategory> data = categoryRepository.findById(id);
		AssertUtil.assertTrue(data.isPresent() && data.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return data.get();
	}

	@Transactional(readOnly = true)
	public Article load(Long merchantId, Long id) {
		Optional<Article> data = repository.findById(id);
		AssertUtil.assertTrue(data.isPresent() && data.get().getMerchant().getId().equals(merchantId),
				ExceptionMessageConstant.NO_SUCH_RECORD);
		return data.get();
	}

	public ArticleCategory save(Long merchantId, ArticleCategoryDTO dto) {
		Optional<Merchant> merchant = merchantRepository.findById(merchantId);
		AssertUtil.assertTrue(merchant.isPresent(), "商户不存在");
		ArticleCategory data = null;
		if (dto.getId() == null) {
			data = new ArticleCategory();
			data.setMerchant(merchant.get());
		} else {
			Optional<ArticleCategory> dataOp = categoryRepository.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
		}
		BeanUtils.copyProperties(dto, data, "id");
		categoryRepository.save(data);
		clearer.clearArticleCategory(data);
		return data;
	}

	public Article save(Long merchantId, ArticleDTO dto) {
		Optional<Merchant> merchant = merchantRepository.findById(merchantId);
		AssertUtil.assertTrue(merchant.isPresent(), "商户不存在");
		Optional<ArticleCategory> category = categoryRepository.findById(dto.getCategoryId());
		AssertUtil.assertTrue(category.isPresent(), "文章分类");
		Article data = null;
		if (dto.getId() == null) {
			data = new Article();
			data.setMerchant(merchant.get());
		} else {
			Optional<Article> dataOp = repository.findById(dto.getId());
			AssertUtil.assertTrue(dataOp.isPresent(), ExceptionMessageConstant.NO_SUCH_RECORD);
			data = dataOp.get();
		}
		data.setCategory(category.get());
		BeanUtils.copyProperties(dto, data, "id", "category", "imgPath");
		repository.save(data);
		if (!StringUtils.isEmpty(dto.getImgPath())) {
			if (dto.getImgPath().startsWith("temp")) {
				String newPath = fileDir + "/" + merchantId + "/" + data.getId() + "/" + System.currentTimeMillis()
						+ ".png";
				data.setImgPath(newPath);
				repository.save(data);
			}
		}
		if (!StringUtils.isEmpty(dto.getVedioPath())) {
			if (dto.getVedioPath().startsWith("temp")) {
				String newPath = fileDir + "/" + merchantId + "/" + data.getId() + "/" + System.currentTimeMillis()
						+ ".mp4";
				data.setVedioPath(newPath);
				repository.save(data);
			}
		}
		clearer.clearArticle(data);
		return data;
	}

	public void deleteCategory(Long merchantId, List<Long> ids) {
		for (Long id : ids) {
			Optional<ArticleCategory> data = categoryRepository.findById(id);
			data.ifPresent(it -> {
				if (it.getMerchant().getId().equals(merchantId)) {
					AssertUtil.assertTrue(repository.countByCategory(it) == 0, "分类" + it.getName() + "底下有文章，请先删除所有文章");
					categoryRepository.delete(it);
					clearer.clearArticleCategory(it);
				}
			});
		}
	}

	public void delete(Long merchantId, List<Long> ids) {
		for (Long id : ids) {
			Optional<Article> data = repository.findById(id);
			data.ifPresent(it -> {
				if (it.getMerchant().getId().equals(merchantId)) {
					repository.delete(it);
					clearer.clearArticle(it);
				}
			});
		}
	}
}
