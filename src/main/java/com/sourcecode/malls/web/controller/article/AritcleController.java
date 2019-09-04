package com.sourcecode.malls.web.controller.article;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sourcecode.malls.domain.article.Article;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.dto.article.ArticleDTO;
import com.sourcecode.malls.dto.base.KeyDTO;
import com.sourcecode.malls.dto.base.ResultBean;
import com.sourcecode.malls.dto.base.SimpleQueryDTO;
import com.sourcecode.malls.dto.query.PageResult;
import com.sourcecode.malls.dto.query.QueryInfo;
import com.sourcecode.malls.repository.jpa.impl.article.ArticleRepository;
import com.sourcecode.malls.service.impl.article.ArticleService;
import com.sourcecode.malls.web.controller.base.BaseController;

@RestController
@RequestMapping(path = "/article")
public class AritcleController extends BaseController {

	@Autowired
	private ArticleService service;

	@Autowired
	private ArticleRepository repository;

	private String fileDir = "article";

	@RequestMapping(path = "/list")
	public ResultBean<PageResult<ArticleDTO>> list(@RequestBody QueryInfo<SimpleQueryDTO> queryInfo) {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.getList(user.getId(), queryInfo));
	}

	@RequestMapping(path = "/load/params/{id}")
	public ResultBean<ArticleDTO> load(@PathVariable Long id) throws Exception {
		User user = getRelatedCurrentUser();
		return new ResultBean<>(service.load(user.getId(), id).asDTO());
	}

	@RequestMapping(path = "/save")
	public ResultBean<Void> save(@RequestBody ArticleDTO dto) {
		User user = getRelatedCurrentUser();
		Article data = service.save(user.getId(), dto);
		if (dto.getImgPath() != null && dto.getImgPath().startsWith("temp")) {
			transfer(true, Arrays.asList(dto.getImgPath()), Arrays.asList(data.getImgPath()));
		}
		if (dto.getVedioPath() != null && dto.getVedioPath().startsWith("temp")) {
			transfer(true, Arrays.asList(dto.getVedioPath()), Arrays.asList(data.getVedioPath()));
		}
		if (dto.getId() == null) {
			dto.setId(0l);
		}
		List<String> tmpPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		String contentDirPath = "temp/" + fileDir + "/content/" + user.getId() + "/" + dto.getId();
		List<String> contentImages = fileService.list(true, contentDirPath);
		for (String image : contentImages) {
			tmpPaths.clear();
			newPaths.clear();
			tmpPaths.add(image);
			String newPath = fileDir + "/" + getRelatedCurrentUser().getId() + "/" + data.getId() + "/content/"
					+ image.replace(contentDirPath + "/", "");
			newPaths.add(newPath);
			transfer(true, true, tmpPaths, newPaths);
			data.setContent(data.getContent().replace(image, newPath));
			fileService.delete(true, image);
			repository.save(data);
		}
		return new ResultBean<>();
	}

	@RequestMapping(path = "/delete")
	public ResultBean<Void> delete(@RequestBody KeyDTO<Long> dto) throws Exception {
		User user = getRelatedCurrentUser();
		service.delete(user.getId(), dto.getIds());
		return new ResultBean<>();
	}

	@RequestMapping(value = "/file/upload/params/{id}")
	public ResultBean<String> settingUpload(@RequestParam("file") MultipartFile file, @PathVariable Long id)
			throws IOException {
		return upload(file, fileDir, id, getRelatedCurrentUser().getId(), false);
	}

	@RequestMapping(value = "/file/load", produces = { MediaType.IMAGE_PNG_VALUE,
			MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public Resource load(@RequestParam String filePath) {
		return load(getRelatedCurrentUser().getId(), filePath, fileDir, false);
	}

	@RequestMapping(value = "/content/image/upload/params/{id}")
	public Map<String, Object> uploadContentImage(@RequestParam("files") List<MultipartFile> files,
			@PathVariable Long id) throws IOException {
		List<String> filePaths = new ArrayList<>();
		for (MultipartFile file : files) {
			filePaths.add(
					upload(file, "temp/" + fileDir + "/content", id, getRelatedCurrentUser().getId(), true).getData());
		}
		Map<String, Object> result = new HashMap<>();
		result.put("errno", 0);
		result.put("data", filePaths);
		return result;
	}

}
