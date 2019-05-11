package com.sourcecode.malls.web.controller;

public enum AuthorityDefinitions {
	GOODS_NOPERMIT_PAGE("商品管理-禁止使用页面", "AUTH_GOODS_NOPERMIT_PAGE", "/Goods/NoPermit/Page", "GET"),
	
	GOODS_BRAND_LIST_PAGE("商品品牌-列表页面", "AUTH_GOODS_BRAND_LIST_PAGE", "/Goods/Brand/List/Page", "GET"),
	GOODS_BRAND_EDIT_PAGE("商品品牌-编辑页面", "AUTH_GOODS_BRAND_EDIT_PAGE", "/Goods/Brand/Edit/Page", "GET"),
	GOODS_BRAND_LIST("商品品牌-列表请求", "AUTH_GOODS_BRAND_LIST", "/goods/brand/list", "POST"),
	GOODS_BRAND_LOAD("商品品牌-单个加载请求", "AUTH_GOODS_BRAND_LOAD", "/goods/brand/load", "GET"),
	GOODS_BRAND_LIST_IN_CATEGORY("商品品牌-分类品牌请求", "AUTH_GOODS_BRAND_LIST_IN_CATEGORY", "/goods/brand/listInCategory", "GET"),
	GOODS_BRAND_SAVE("商品品牌-保存请求", "AUTH_GOODS_BRAND_SAVE", "/goods/brand/save", "POST"),
	GOODS_BRAND_DELETE("商品品牌-删除请求", "AUTH_GOODS_BRAND_DELETE", "/goods/brand/delete", "POST"),
	GOODS_BRAND_FILE_UPLOAD("商品品牌-文件上传请求", "AUTH_GOODS_BRAND_FILE_UPLOAD", "/goods/brand/file/upload", "POST"),
	GOODS_BRAND_FILE_LOAD("商品品牌-文件读取请求", "AUTH_GOODS_BRAND_FILE_LOAD", "/goods/brand/file/load", "GET"),
	
	GOODS_CATEGORY_LIST_PAGE("商品分类-列表页面", "AUTH_GOODS_CATEGORY_LIST_PAGE", "/Goods/Category/List/Page", "GET"),
	GOODS_CATEGORY_EDIT_PAGE("商品分类-编辑页面", "AUTH_GOODS_CATEGORY_EDIT_PAGE", "/Goods/Category/Edit/Page", "GET"),
	GOODS_CATEGORY_LIST("商品分类-列表请求", "AUTH_GOODS_CATEGORY_LIST", "/goods/category/list", "POST"),
	GOODS_CATEGORY_LOAD("商品分类-单个加载请求", "AUTH_GOODS_CATEGORY_LOAD", "/goods/category/load", "GET"),
	GOODS_CATEGORY_LIST_ALL("商品品牌-分类请求", "AUTH_GOODS_CATEGORY_LIST_ALL", "/goods/category/list/all", "GET"),
	GOODS_CATEGORY_LIST_ALL_PARENTS("商品品牌-父节点请求", "AUTH_GOODS_CATEGORY_LIST_ALL_PARENTS", "/goods/category/list/allParents", "GET"),
	GOODS_CATEGORY_SAVE("商品分类-保存请求", "AUTH_GOODS_CATEGORY_SAVE", "/goods/category/save", "POST"),
	GOODS_CATEGORY_DELETE("商品分类-删除请求", "AUTH_GOODS_CATEGORY_DELETE", "/goods/category/delete", "POST"),
	GOODS_CATEGORY_FILE_UPLOAD("商品分类-文件上传请求", "AUTH_GOODS_CATEGORY_FILE_UPLOAD", "/goods/category/file/upload", "POST"),
	GOODS_CATEGORY_FILE_LOAD("商品分类-文件读取请求", "AUTH_GOODS_CATEGORY_FILE_LOAD", "/goods/category/file/load", "GET"),
	
	GOODS_ITEM_INDEX_PAGE("商品-索引页面", "AUTH_GOODS_ITEM_INDEX_PAGE", "/Goods/Item/Index/Page", "GET"),
	GOODS_ITEM_LIST_PAGE("商品-列表页面", "AUTH_GOODS_ITEM_LIST_PAGE", "/Goods/Item/List/Page", "GET"),
	GOODS_ITEM_EDIT_PAGE("商品-编辑页面", "AUTH_GOODS_ITEM_EDIT_PAGE", "/Goods/Item/Edit/Page", "GET"),
	GOODS_ITEM_LIST("商品-列表请求", "AUTH_GOODS_ITEM_LIST", "/goods/item/list", "POST"),
	GOODS_ITEM_LOAD("商品-单个加载请求", "AUTH_GOODS_ITEM_LOAD", "/goods/item/load", "GET"),
	GOODS_ITEM_SAVE("商品-保存请求", "AUTH_GOODS_ITEM_SAVE", "/goods/item/save", "POST"),
	GOODS_ITEM_PROPERTIES_SAVE("商品-保存规格请求", "AUTH_GOODS_ITEM_PROPERTIES_SAVE", "/goods/item/properties/save", "POST"),
	GOODS_ITEM_UPDATE_STATUS("商品-更新状态请求", "AUTH_GOODS_ITEM_UPDATE_STATUS", "/goods/item/updateStatus", "POST"),
	GOODS_ITEM_DELETE("商品-删除请求", "AUTH_GOODS_ITEM_DELETE", "/goods/item/delete", "POST"),
	GOODS_ITEM_FILE_UPLOAD("商品-文件上传请求", "AUTH_GOODS_ITEM_FILE_UPLOAD", "/goods/item/file/upload", "POST"),
	GOODS_ITEM_FILE_LOAD("商品-文件读取请求", "AUTH_GOODS_ITEM_FILE_LOAD", "/goods/item/file/load", "GET"),
	
	GOODS_SPECIFICATION_DEFINITION_INDEX_PAGE("商品规格-索引页面", "AUTH_GOODS_SPECIFICATION_DEFINITION_INDEX_PAGE", "/Goods/Specification/Definition/Index/Page", "GET"),
	GOODS_SPECIFICATION_DEFINITION_LIST_PAGE("商品规格-列表页面", "AUTH_GOODS_SPECIFICATION_DEFINITION_LIST_PAGE", "/Goods/Specification/Definition/List/Page", "GET"),
	GOODS_SPECIFICATION_DEFINITION_EDIT_PAGE("商品规格-编辑页面", "AUTH_GOODS_SPECIFICATION_DEFINITION_EDIT_PAGE", "/Goods/Specification/Definition/Edit/Page", "GET"),
	GOODS_SPECIFICATION_DEFINITION_RELATED_PAGE("商品规格-关联页面", "AUTH_GOODS_SPECIFICATION_DEFINITION_RELATED_PAGE", "/Goods/Specification/Definition/Related/Page", "GET"),
	GOODS_SPECIFICATION_DEFINITION_RELATE("商品规格-关联请求", "AUTH_GOODS_SPECIFICATION_DEFINITION_RELATE", "/goods/specification/definition/relate", "POST"),
	GOODS_SPECIFICATION_DEFINITION_LIST("商品规格-列表请求", "AUTH_GOODS_SPECIFICATION_DEFINITION_LIST", "/goods/specification/definition/list", "POST"),
	GOODS_SPECIFICATION_DEFINITION_GROUPS("商品规格-类型请求", "AUTH_GOODS_SPECIFICATION_DEFINITION_GROUPS", "/goods/specification/definition/groups", "GET"),
	GOODS_SPECIFICATION_DEFINITION_LIST_IN_GROUP("商品规格-类型规格请求", "AUTH_GOODS_SPECIFICATION_DEFINITION_LIST_IN_GROUP", "/goods/specification/definition/listInGroup", "GET"),
	GOODS_SPECIFICATION_DEFINITION_LOAD("商品规格-单个加载请求", "AUTH_GOODS_SPECIFICATION_DEFINITION_LOAD", "/goods/specification/definition/load", "GET"),
	GOODS_SPECIFICATION_DEFINITION_SAVE("商品规格-保存请求", "AUTH_GOODS_SPECIFICATION_DEFINITION_SAVE", "/goods/specification/definition/save", "POST"),
	GOODS_SPECIFICATION_DEFINITION_DELETE("商品规格-删除请求", "AUTH_GOODS_SPECIFICATION_DEFINITION_DELETE", "/goods/specification/definition/delete", "POST"),
	
	GOODS_SPECIFICATION_GROUP_INDEX_PAGE("商品类型-索引页面", "AUTH_GOODS_SPECIFICATION_GROUP_INDEX_PAGE", "/Goods/Specification/Group/Index/Page", "GET"),
	GOODS_SPECIFICATION_GROUP_LIST_PAGE("商品类型-列表页面", "AUTH_GOODS_SPECIFICATION_GROUP_LIST_PAGE", "/Goods/Specification/Group/List/Page", "GET"),
	GOODS_SPECIFICATION_GROUP_EDIT_PAGE("商品类型-编辑页面", "AUTH_GOODS_SPECIFICATION_GROUP_EDIT_PAGE", "/Goods/Specification/Group/Edit/Page", "GET"),
	GOODS_SPECIFICATION_GROUP_LIST("商品类型-列表请求", "AUTH_GOODS_SPECIFICATION_GROUP_LIST", "/goods/specification/group/list", "POST"),
	GOODS_SPECIFICATION_GROUP_LIST_IN_CATEGORY("商品类型-分类类型请求", "AUTH_GOODS_SPECIFICATION_GROUP_LIST_IN_CATEGORY", "/goods/specification/group/listInCategory", "GET"),
	GOODS_SPECIFICATION_GROUP_LOAD("商品类型-单个加载请求", "AUTH_GOODS_SPECIFICATION_GROUP_LOAD", "/goods/specification/group/load", "GET"),
	GOODS_SPECIFICATION_GROUP_SAVE("商品类型-保存请求", "AUTH_GOODS_SPECIFICATION_GROUP_SAVE", "/goods/specification/group/save", "POST"),
	GOODS_SPECIFICATION_GROUP_DELETE("商品类型-删除请求", "AUTH_GOODS_SPECIFICATION_GROUP_DELETE", "/goods/specification/group/delete", "POST"),
	
	MERCHANT_SHOP_APPLICATION_INDEX_PAGE("实名认证-索引页面", "AUTH_MERCHANT_SHOP_APPLICATION_INDEX_PAGE", "/Merchant/Shop/Application/Index/Page", "GET"),
	MERCHANT_SHOP_APPLICATION_NOPERMIT_PAGE("实名认证-禁止页面", "AUTH_MERCHANT_SHOP_APPLICATION_NOPERMIT_PAGE", "/Merchant/Shop/Application/NoPermit/Page", "GET"),
	MERCHANT_SHOP_APPLICATION_APPLY_PAGE("店铺申请-申请页面", "AUTH_MERCHANT_SHOP_APPLICATION_APPLY_PAGE", "/Merchant/Shop/Application/Apply/Page", "GET"),
	MERCHANT_SHOP_APPLICATION_COMMIT_SUCCESS_PAGE("店铺申请-提交成功页面", "AUTH_MERCHANT_SHOP_APPLICATION_COMMIT_SUCCESS_PAGE", "/Merchant/ShopApplication/CommitSuccess/Page", "GET"),
	MERCHANT_SHOP_APPLICATION_PASSED_PAGE("店铺申请-申请通过页面", "AUTH_MERCHANT_SHOP_APPLICATION_PASSED_PAGE", "/Merchant/Shop/Application/Passed/Page", "GET"),
	MERCHANT_SHOP_APPLICATION_UNPASSED_PAGE("店铺申请-申请不通过页面", "AUTH_MERCHANT_SHOP_APPLICATION_UNPASSED_PAGE", "/Merchant/Shop/Application/UnPassed/Page", "GET"),
	MERCHANT_SHOP_APPLICATION_DETAIL_PAGE("店铺申请-详情页面", "AUTH_MERCHANT_SHOP_APPLICATION_DETAIL_PAGE", "/Merchant/Shop/Application/Detail/Page", "GET"),
	MERCHANT_SHOP_APPLICATION_EDIT_PAGE("店铺申请-编辑页面", "AUTH_MERCHANT_SHOP_APPLICATION_EDIT_PAGE", "/Merchant/Shop/Application/Edit/Page", "GET"),
	MERCHANT_SHOP_APPLICATION_LOAD("店铺申请-加载请求", "AUTH_MERCHANT_SHOP_APPLICATION_LOAD", "/merchant/shop/application/load", "GET"),
	MERCHANT_SHOP_APPLICATION_APPLY("店铺申请-提交申请请求", "AUTH_MERCHANT_SHOP_APPLICATION_APPLY", "/merchant/shop/application/apply", "POST"),
	MERCHANT_SHOP_APPLICATION_UPDATE("店铺申请-编辑更新请求", "AUTH_MERCHANT_SHOP_APPLICATION_UPDATE", "/merchant/shop/application/update", "POST"),
	MERCHANT_SHOP_APPLICATION_FILE_UPLOAD("店铺申请-文件上传请求", "AUTH_MERCHANT_SHOP_APPLICATION_FILE_UPLOAD", "/merchant/shop/application/file/upload", "POST"),
	MERCHANT_SHOP_APPLICATION_FILE_LOAD("店铺申请-文件读取请求", "AUTH_MERCHANT_SHOP_APPLICATION_FILE_LOAD", "/merchant/shop/application/file/load", "GET"),
	
	MERCHANT_SUB_ACCOUNT_LIST_PAGE("子账号-列表页面", "AUTH_MERCHANT_SUB_ACCOUNT_LIST_PAGE", "/Merchant/SubAccount/List/Page", "GET"),
	MERCHANT_SUB_ACCOUNT_EDIT_PAGE("子账号-编辑页面", "AUTH_MERCHANT_SUB_ACCOUNT_EDIT_PAGE", "/Merchant/SubAccount/Edit/Page", "GET"),
	MERCHANT_SUB_ACCOUNT_LIST("子账号-列表请求", "AUTH_MERCHANT_SUB_ACCOUNT_LIST", "/merchant/subAccount/list", "POST"),
	MERCHANT_SUB_ACCOUNT_LOAD("子账号-单个加载请求", "AUTH_MERCHANT_SUB_ACCOUNT_LOAD", "/merchant/subAccount/load", "GET"),
	MERCHANT_SUB_ACCOUNT_AUTHORITIES("子账号-权限列表请求", "AUTH_MERCHANT_SUB_ACCOUNT_AUTHORITIES", "/merchant/subAccount/authorities", "GET"),
	MERCHANT_SUB_ACCOUNT_SAVE("子账号-保存请求", "AUTH_MERCHANT_SUB_ACCOUNT_SAVE", "/merchant/subAccount/save", "POST"),
	MERCHANT_SUB_ACCOUNT_DELETE("子账号-删除请求", "AUTH_MERCHANT_SUB_ACCOUNT_DELETE", "/merchant/subAccount/delete", "POST"),
	MERCHANT_SUB_ACCOUNT_UPDATE_STATUS("子账号-更新状态请求", "AUTH_MERCHANT_SUB_ACCOUNT_UPDATE_STATUS", "/merchant/subAccount/updateStatus", "POST"),
	MERCHANT_SUB_ACCOUNT_FILE_UPLOAD("子账号-文件上传请求", "AUTH_MERCHANT_SUB_ACCOUNT_FILE_UPLOAD", "/merchant/subAccount/file/upload", "POST"),
	MERCHANT_SUB_ACCOUNT_FILE_LOAD("子账号-文件读取请求", "AUTH_MERCHANT_SUB_ACCOUNT_FILE_LOAD", "/merchant/subAccount/file/load", "GET"),
	
	MERCHANT_USER_PROFILE_PAGE("商家信息-账户信息页面", "AUTH_MERCHANT_USER_PROFILE_PAGE", "/Merchant/User/Profile/Page", "GET"),
	
	MERCHANT_VERIFICATION_INDEX_PAGE("实名认证-索引页面", "AUTH_MERCHANT_VERIFICATION_INDEX_PAGE", "/Merchant/Verification/Index/Page", "GET"),
	MERCHANT_VERIFICATION_VERIFY_PAGE("实名认证-申请页面", "AUTH_MERCHANT_VERIFICATION_VERIFY_PAGE", "/Merchant/Verification/Verify/Page", "GET"),
	MERCHANT_VERIFICATION_COMMIT_SUCCESS_PAGE("实名认证-提交成功页面", "AUTH_MERCHANT_VERIFICATION_COMMIT_SUCCESS_PAGE", "/Merchant/Verification/CommitSuccess/Page", "GET"),
	MERCHANT_VERIFICATION_UNPASSED_PAGE("实名认证-审核失败页面", "AUTH_MERCHANT_VERIFICATION_UNPASSED_PAGE", "/Merchant/Verification/UnPassed/Page", "GET"),
	MERCHANT_VERIFICATION_EDIT_PAGE("实名认证-编辑页面", "AUTH_MERCHANT_VERIFICATION_EDIT_PAGE", "/Merchant/Verification/Edit/Page", "GET"),
	MERCHANT_VERIFICATION_LOAD("实名认证-加载请求", "AUTH_MERCHANT_SVERIFICATION_LOAD", "/merchant/verification/load", "GET"),
	MERCHANT_VERIFICATION_VERIFY("实名认证-提交申请请求", "AUTH_MERCHANT_VERIFICATION_APPLY", "/merchant/verification/verify", "POST"),
	MERCHANT_VERIFICATION_UPDATE("实名认证-编辑更新请求", "AUTH_MERCHANT_VERIFICATION_UPDATE", "/merchant/verification/update", "POST"),
	MERCHANT_VERIFICATION_FILE_UPLOAD("实名认证-文件上传请求", "AUTH_MERCHANT_VERIFICATION_FILE_UPLOAD", "/merchant/verification/file/upload", "POST"),
	MERCHANT_VERIFICATION_FILE_LOAD("实名认证-文件读取请求", "AUTH_MERCHANT_VERIFICATION_FILE_LOAD", "/merchant/verification/file/load", "GET"),
	;
	
	private String name;
	private String code;
	private String link;
	private String method;

	private AuthorityDefinitions(String name, String code, String link, String method) {
		this.name = name;
		this.code = code;
		this.link = link;
		this.method = method;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getLink() {
		return link;
	}
	
	public String getMethod() {
		return method;
	}
	
}
