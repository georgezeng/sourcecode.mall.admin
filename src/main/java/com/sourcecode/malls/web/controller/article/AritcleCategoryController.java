package com.sourcecode.malls.web.controller.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.article.ArticleCategoryDTO;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.service.impl.article.ArticleService;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/article/category")
public class AritcleCategoryController extends BaseController {

	@Autowired
	private ArticleService service;

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<ArticleCategoryDTO>> list(@RequestBody QueryInfo<SimpleQueryDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getCategoryList(user.getId(), queryInfo));
	}

	@RequestMapping(path = "/list/all")
	public ResultBean<ArticleCategoryDTO> listAll() {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getAllCategory(user.getId()));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<ArticleCategoryDTO> load(@PathVariable Long id) throws Exception {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.loadCategory(user.getId(), id).asDTO());
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody ArticleCategoryDTO dto) {
		User user = getRelatedCurrentUser();
		service.save(user.getId(), dto);
		return new ResultBean<>();
	}

	@RequestMapping(path = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> dto) throws Exception {
		User user = getRelatedCurrentUser();
		service.deleteCategory(user.getId(), dto.getIds());
		return new ResultBean<>();
	}

}
