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
	GOODS_ITEM_CONTENT_IMAGE_UPLOAD("商品-内容图片上传请求", "AUTH_GOODS_ITEM_CONTENT_IMAGE_UPLOAD", "/goods/item/content/image/upload", "POST"),
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
	
	MERCHANT_SHOP_APPLICATION_INDEX_PAGE("店铺申请-索引页面", "AUTH_MERCHANT_SHOP_APPLICATION_INDEX_PAGE", "/Merchant/Shop/Application/Index/Page", "GET", true),
	MERCHANT_SHOP_APPLICATION_NOPERMIT_PAGE("店铺申请-禁止页面", "AUTH_MERCHANT_SHOP_APPLICATION_NOPERMIT_PAGE", "/Merchant/Shop/Application/NoPermit/Page", "GET", true),
	MERCHANT_SHOP_APPLICATION_APPLY_PAGE("店铺申请-申请页面", "AUTH_MERCHANT_SHOP_APPLICATION_APPLY_PAGE", "/Merchant/Shop/Application/Apply/Page", "GET", true),
	MERCHANT_SHOP_APPLICATION_COMMIT_SUCCESS_PAGE("店铺申请-提交成功页面", "AUTH_MERCHANT_SHOP_APPLICATION_COMMIT_SUCCESS_PAGE", "/Merchant/ShopApplication/CommitSuccess/Page", "GET", true),
	MERCHANT_SHOP_APPLICATION_PASSED_PAGE("店铺申请-申请通过页面", "AUTH_MERCHANT_SHOP_APPLICATION_PASSED_PAGE", "/Merchant/Shop/Application/Passed/Page", "GET", true),
	MERCHANT_SHOP_APPLICATION_UNPASSED_PAGE("店铺申请-申请不通过页面", "AUTH_MERCHANT_SHOP_APPLICATION_UNPASSED_PAGE", "/Merchant/Shop/Application/UnPassed/Page", "GET", true),
	MERCHANT_SHOP_APPLICATION_DETAIL_PAGE("店铺申请-详情页面", "AUTH_MERCHANT_SHOP_APPLICATION_DETAIL_PAGE", "/Merchant/Shop/Application/Detail/Page", "GET", true),
	MERCHANT_SHOP_APPLICATION_EDIT_PAGE("店铺申请-编辑页面", "AUTH_MERCHANT_SHOP_APPLICATION_EDIT_PAGE", "/Merchant/Shop/Application/Edit/Page", "GET", true),
	MERCHANT_SHOP_APPLICATION_LOAD("店铺申请-加载请求", "AUTH_MERCHANT_SHOP_APPLICATION_LOAD", "/merchant/shop/application/load", "GET", true),
	MERCHANT_SHOP_APPLICATION_APPLY("店铺申请-提交申请请求", "AUTH_MERCHANT_SHOP_APPLICATION_APPLY", "/merchant/shop/application/apply", "POST", true),
	MERCHANT_SHOP_APPLICATION_UPDATE("店铺申请-编辑更新请求", "AUTH_MERCHANT_SHOP_APPLICATION_UPDATE", "/merchant/shop/application/update", "POST", true),
	MERCHANT_SHOP_APPLICATION_FILE_UPLOAD("店铺申请-文件上传请求", "AUTH_MERCHANT_SHOP_APPLICATION_FILE_UPLOAD", "/merchant/shop/application/file/upload", "POST", true),
	MERCHANT_SHOP_APPLICATION_FILE_LOAD("店铺申请-文件读取请求", "AUTH_MERCHANT_SHOP_APPLICATION_FILE_LOAD", "/merchant/shop/application/file/load", "GET", true),
	
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
	
	MERCHANT_USER_PROFILE_PAGE("商家信息-账户信息页面", "AUTH_MERCHANT_USER_PROFILE_PAGE", "/Merchant/User/Profile/Page", "GET", true),
	
	MERCHANT_VERIFICATION_INDEX_PAGE("实名认证-索引页面", "AUTH_MERCHANT_VERIFICATION_INDEX_PAGE", "/Merchant/Verification/Index/Page", "GET", true),
	MERCHANT_VERIFICATION_VERIFY_PAGE("实名认证-申请页面", "AUTH_MERCHANT_VERIFICATION_VERIFY_PAGE", "/Merchant/Verification/Verify/Page", "GET", true),
	MERCHANT_VERIFICATION_COMMIT_SUCCESS_PAGE("实名认证-提交成功页面", "AUTH_MERCHANT_VERIFICATION_COMMIT_SUCCESS_PAGE", "/Merchant/Verification/CommitSuccess/Page", "GET", true),
	MERCHANT_VERIFICATION_UNPASSED_PAGE("实名认证-审核失败页面", "AUTH_MERCHANT_VERIFICATION_UNPASSED_PAGE", "/Merchant/Verification/UnPassed/Page", "GET", true),
	MERCHANT_VERIFICATION_EDIT_PAGE("实名认证-编辑页面", "AUTH_MERCHANT_VERIFICATION_EDIT_PAGE", "/Merchant/Verification/Edit/Page", "GET", true),
	MERCHANT_VERIFICATION_LOAD("实名认证-加载请求", "AUTH_MERCHANT_SVERIFICATION_LOAD", "/merchant/verification/load", "GET", true),
	MERCHANT_VERIFICATION_VERIFY("实名认证-提交申请请求", "AUTH_MERCHANT_VERIFICATION_APPLY", "/merchant/verification/verify", "POST", true),
	MERCHANT_VERIFICATION_UPDATE("实名认证-编辑更新请求", "AUTH_MERCHANT_VERIFICATION_UPDATE", "/merchant/verification/update", "POST", true),
	MERCHANT_VERIFICATION_FILE_UPLOAD("实名认证-文件上传请求", "AUTH_MERCHANT_VERIFICATION_FILE_UPLOAD", "/merchant/verification/file/upload", "POST", true),
	MERCHANT_VERIFICATION_FILE_LOAD("实名认证-文件读取请求", "AUTH_MERCHANT_VERIFICATION_FILE_LOAD", "/merchant/verification/file/load", "GET", true),
	
	WECHAT_SETTING_PAGE("微信配置-配置页面", "AUTH_WECHAT_SETTING_PAGE", "/Setting/Wechat/Page", "GET"),
	WECHAT_SETTING_LOAD_GZH("微信配置-加载请求", "AUTH_WECHAT_SETTING_LOAD_GZH", "/setting/wechat/gzh/load", "GET"),
	WECHAT_SETTING_SAVE_GZH("微信配置-保存请求", "AUTH_WECHAT_SETTING_SAVE_GZH", "/setting/wechat/gzh/save", "POST"),
	WECHAT_SETTING_UPLOAD_PAY_CERT("微信配置-上传支付证书", "AUTH_WECHAT_SETTING_UPLOAD_PAY_CERT", "/setting/wechat/pay/cert/upload", "POST"),
	
	ALIPAY_SETTING_PAGE("支付宝配置-配置页面", "AUTH_ALIPAY_SETTING_PAGE", "/Setting/Alipay/Page", "GET"),
	ALIPAY_SETTING_LOAD("支付宝配置-加载请求", "AUTH_ALIPAY_SETTING_LOAD", "/setting/alipay/load", "GET"),
	ALIPAY_SETTING_SAVE("支付宝配置-保存请求", "AUTH_ALIPAY_SETTING_SAVE", "/setting/alipay/save", "POST"),
	
	CLIENT_IDENTITY_LIST_PAGE("会员认证-列表页面", "AUTH_CLIENT_IDENTITY_LIST_PAGE", "/Client/Identity/List/Page", "GET"),
	CLIENT_IDENTITY_EDIT_PAGE("会员认证-编辑页面", "AUTH_CLIENT_IDENTITY_EDIT_PAGE", "/Client/Identity/Edit/Page", "GET"),
	CLIENT_IDENTITY_LIST("会员认证-列表请求", "AUTH_CLIENT_IDENTITY_LIST", "/client/identity/list", "POST"),
	CLIENT_IDENTITY_LOAD("会员认证-加载请求", "AUTH_CLIENT_IDENTITY_LOAD", "/client/identity/load", "GET"),
	CLIENT_IDENTITY_FILE_LOAD("会员认证-文件请求", "AUTH_CLIENT_IDENTITY_FILE_LOAD", "/client/identity/file/load", "GET"),
	CLIENT_IDENTITY_UPDATE_STATUS("会员认证-修改审核状态", "AUTH_CLIENT_IDENTITY_UPDATE_STATUS", "/client/identity/updateStatus", "POST"),
	
	CLIENT_USER_LIST_PAGE("会员列表-列表页面", "AUTH_CLIENT_USER_LIST_PAGE", "/Client/User/List/Page", "GET"),
	CLIENT_USER_EDIT_PAGE("会员列表-编辑页面", "AUTH_CLIENT_USER_EDIT_PAGE", "/Client/User/Edit/Page", "GET"),
	CLIENT_USER_LIST("会员列表-列表请求", "AUTH_CLIENT_USER_LIST", "/client/user/list", "POST"),
	CLIENT_USER_LOAD("会员列表-加载请求", "AUTH_CLIENT_USER_LOAD", "/client/user/load", "GET"),
	CLIENT_USER_FILE_LOAD("会员列表-文件请求", "AUTH_CLIENT_USER_FILE_LOAD", "/client/user/file/load", "GET"),
	CLIENT_USER_UPDATE_STATUS("会员列表-修改状态", "AUTH_CLIENT_USER_UPDATE_STATUS", "/client/user/updateStatus", "POST"),
	
	INVOICE_SETTING_LIST_PAGE("发票设置-列表页面", "AUTH_INVOICE_SETTING_LIST_PAGE", "/Invoice/Setting/List/Page", "GET"),
	INVOICE_SETTING_EDIT_PAGE("发票设置-编辑页面", "AUTH_INVOICE_SETTING_EDIT_PAGE", "/Invoice/Setting/Edit/Page", "GET"),
	INVOICE_SETTING_LIST("发票设置-列表请求", "AUTH_INVOICE_SETTING_LIST", "/invoice/setting/list", "POST"),
	INVOICE_SETTING_LOAD("发票设置-加载请求", "AUTH_INVOICE_SETTING_LOAD", "/invoice/setting/load", "GET"),
	INVOICE_SETTING_SAVE("发票设置-保存请求", "AUTH_INVOICE_SETTING_SAVE", "/invoice/setting/save", "POST"),
	INVOICE_SETTING_DELETE("发票设置-删除请求", "AUTH_INVOICE_SETTING_DELETE", "/invoice/setting/delete", "GET"),
	
	AFTERSALE_REASON_SETTING_LIST_PAGE("售后原因设置-列表页面", "AUTH_AFTERSALE_REASON_SETTING_LIST_PAGE", "/AfterSale/Reason/Setting/List/Page", "GET"),
	AFTERSALE_REASON_SETTING_EDIT_PAGE("售后原因设置-编辑页面", "AUTH_AFTERSALE_REASON_SETTING_EDIT_PAGE", "/AfterSale/Reason/Setting/Edit/Page", "GET"),
	AFTERSALE_REASON_SETTING_LIST("售后原因设置-列表请求", "AUTH_AFTERSALE_REASON_SETTING_LIST", "/afterSale/reason/setting/list", "POST"),
	AFTERSALE_REASON_SETTING_LOAD("售后原因设置-加载请求", "AUTH_AFTERSALE_REASON_SETTING_LOAD", "/afterSale/reason/setting/load", "GET"),
	AFTERSALE_REASON_SETTING_SAVE("售后原因设置-保存请求", "AUTH_AFTERSALE_REASON_SETTING_SAVE", "/afterSale/reason/setting/save", "POST"),
	AFTERSALE_REASON_SETTING_DELETE("售后原因设置-删除请求", "AUTH_AFTERSALE_REASON_SETTING_DELETE", "/afterSale/reason/setting/delete", "GET"),
	
	AFTERSALE_REFUND_ONLY_LIST_PAGE("仅退款-列表页面", "AUTH_AFTERSALE_REFUND_ONLY_LIST_PAGE", "/AfterSale/RefundOnly/List/Page", "GET"),
	AFTERSALE_REFUND_ONLY_DETAIL_PAGE("仅退款-详情页面", "AUTH_AFTERSALE_REFUND_ONLY_DETAIL_PAGE", "/AfterSale/RefundOnly/Detail/Page", "GET"),
	
	AFTERSALE_SALES_RETURN_LIST_PAGE("退货退款-列表页面", "AUTH_AFTERSALE_SALES_RETURN_LIST_PAGE", "/AfterSale/SalesReturn/List/Page", "GET"),
	AFTERSALE_SALES_RETURN_DETAIL_PAGE("退货退款-详情页面", "AUTH_AFTERSALE_SALES_RETURN_DETAIL_PAGE", "/AfterSale/SalesReturn/Detail/Page", "GET"),
	
	AFTERSALE_CHANGE_LIST_PAGE("换货-列表页面", "AUTH_AFTERSALE_CHANGE_LIST_PAGE", "/AfterSale/Change/List/Page", "GET"),
	AFTERSALE_CHANGE_DETAIL_PAGE("换货-详情页面", "AUTH_AFTERSALE_CHANGE_DETAIL_PAGE", "/AfterSale/Change/Detail/Page", "GET"),
	
	AFTERSALE_APPLICATION_LIST("申请售后-列表请求", "AUTH_AFTERSALE_APPLICATION_LIST", "/afterSale/application/list", "POST"),
	AFTERSALE_APPILCATION_LOAD("申请售后-加载请求", "AUTH_AFTERSALE_APPILCATION_LOAD", "/afterSale/application/load", "GET"),
	AFTERSALE_APPLICATION_AUDIT("申请售后-审核请求", "AUTH_AFTERSALE_APPLICATION_AUDIT", "/afterSale/application/audit", "POST"),
	AFTERSALE_APPLICATION_REFUND("申请售后-退款请求", "AUTH_AFTERSALE_APPLICATION_REFUND", "/afterSale/application/refund", "POST"),
	AFTERSALE_APPLICATION_RECEIVE("申请售后-确认收货请求", "AUTH_AFTERSALE_APPLICATION_RECEIVE", "/afterSale/application/receive", "GET"),
	AFTERSALE_APPLICATION_SENT("申请售后-发货请求", "AUTH_AFTERSALE_APPLICATION_SENT", "/afterSale/application/sent", "POST"),
	
	AFTERSALE_RETURN_ADDRESS_PAGE("回寄地址设置-设置页面", "AUTH_AFTERSALE_RETURN_ADDRESS_PAGE", "/AfterSale/ReturnAddress/Page", "GET"),
	
	GOODS_ITEM_EVALUATION_LIST_PAGE("商品评价-列表页面", "AUTH_GOODS_ITEM_EVALUATION_LIST_PAGE", "/Evaluation/List/Page", "GET"),
	GOODS_ITEM_EVALUATION_EDIT_PAGE("商品评价-编辑页面", "AUTH_GOODS_ITEM_EVALUATION_EDIT_PAGE", "/Evaluation/Edit/Page", "GET"),
	GOODS_ITEM_EVALUATION_LIST("商品评价-列表请求", "AUTH_GOODS_ITEM_EVALUATION_LIST", "/evaluation/list", "POST"),
	GOODS_ITEM_EVALUATION_LOAD("商品评价-加载请求", "AUTH_GOODS_ITEM_EVALUATION_LOAD", "/evaluation/load", "GET"),
	GOODS_ITEM_EVALUATION_AUDIT("商品评价-审核请求", "AUTH_GOODS_ITEM_EVALUATION_AUDIT", "/evaluation/audit", "POST"),
	GOODS_ITEM_EVALUATION_REPLY("商品评价-回复请求", "AUTH_GOODS_ITEM_EVALUATION_REPLY", "/evaluation/reply", "POST"),
	GOODS_ITEM_EVALUATION_OPEN("商品评价-是否显示请求", "AUTH_GOODS_ITEM_EVALUATION_OPEN", "/evaluation/open", "POST"),
	
	MERCHANT_SETTING_LOAD("商家参数设置-加载请求", "AUTH_MERCHANT_SETTING_LOAD", "/merchant/setting/load", "GET"),
	MERCHANT_SETTING_SAVE("商家参数设置-保存请求", "AUTH_MERCHANT_SETTING_SAVE", "/merchant/setting/save", "POST"),
	
	ORDER_LIST_PAGE("订单-列表页面", "AUTH_ORDER_LIST_PAGE", "/Order/List/Page", "GET"),
	ORDER_REFUND_LIST_PAGE("订单-退款列表", "AUTH_ORDER_REFUND_LIST_PAGE", "/Order/Refund/List/Page", "GET"),
	ORDER_EDIT_PAGE("订单-编辑页面", "AUTH_ORDER_EDIT_PAGE", "/Order/Edit/Page", "GET"),
	ORDER_EXPRESS_PAGE("订单-物流页面", "AUTH_ORDER_EXPRESS_PAGE", "/Order/Express/Page", "GET"),
	ORDER_LIST("订单-列表请求", "AUTH_ORDER_LIST", "/order/list", "POST"),
	ORDER_REFUND_LIST("订单-退款列表请求", "AUTH_ORDER_REFUND_LIST", "/order/refund/list", "POST"),
	ORDER_LOAD("订单-加载请求", "AUTH_ORDER_LOAD", "/order/load", "GET"),
	ORDER_UPDATE_EXPRESS("订单-更新物流", "AUTH_ORDER_UPDATE_EXPRESS", "/order/updateExpress", "POST"),
	ORDER_APPROVE_REFUND("订单-同意退款", "AUTH_ORDER_APPROVE_REFUND", "/order/refund/approve", "GET"),
	
	CASH_COUPON_SETTING_LIST_PAGE("现金券-列表页面", "AUTH_CASH_COUPON_SETTING_LIST_PAGE", "/Coupon/Cash/Setting/List/Page", "GET"),
	CASH_COUPON_SETTING_EDIT_PAGE("现金券-编辑页面", "AUTH_CASH_COUPON_SETTING_EDIT_PAGE", "/Coupon/Cash/Setting/Edit/Page", "GET"),
	
	COUPON_CLIENT_LIST_PAGE("现金券-用户领券列表页面", "AUTH_COUPON_CLIENT_LIST_PAGE", "/Coupon/Cash/Client/List/Page", "GET"),
	COUPON_CLIENT_LIST("现金券-列表请求", "AUTH_COUPON_CLIENT_LIST", "/coupon/client/list", "POST"),
	COUPON_SETTING_LIST("现金券-列表请求", "AUTH_COUPON_SETTING_LIST", "/coupon/setting/list", "POST"),
	COUPON_SETTING_LOAD("现金券-加载请求", "AUTH_COUPON_SETTING_LOAD", "/coupon/setting/load", "GET"),
	COUPON_SETTING_FILE_UPLOAD("现金券-上传文件请求", "AUTH_COUPON_SETTING_FILE_UPLOAD", "/coupon/setting/file/upload", "POST"),
	COUPON_SETTING_SAVE_BASE_INFO("现金券-保存基本信息请求", "AUTH_COUPON_SETTING_SAVE_BASE_INFO", "/coupon/setting/save/baseInfo", "POST"),
	COUPON_SETTING_SAVE_CONDITION_ZS("现金券-保存赠送条件请求", "AUTH_COUPON_SETTING_SAVE_CONDITION_ZS", "/coupon/setting/save/condition/zs", "POST"),
	COUPON_SETTING_SAVE_CONDITION_HX("现金券-保存核销条件请求", "AUTH_COUPON_SETTING_SAVE_CONDITION_HX", "/coupon/setting/save/condition/hx", "POST"),
	COUPON_SETTING_UPDATE_STATUS("现金券-更新状态请求", "AUTH_COUPON_SETTING_UPDATE_STATUS", "/coupon/setting/updateStatus", "POST"),
	COUPON_SETTING_DELETE("现金券-删除请求", "AUTH_COUPON_SETTING_DELETE", "/coupon/setting/delete", "POST"),
	COUPON_SETTING_FILE_LOAD("现金券-文件请求", "AUTH_COUPON_SETTING_FILE_LOAD", "/coupon/setting/file/load", "GET"),
	
	CASH_COUPON_ORDER_LIMITED_SETTING_LIST_PAGE("现金券限额配置-列表页面", "AUTH_CASH_COUPON_ORDER_LIMITED_SETTING_LIST_PAGE", "/Coupon/Cash/OrderLimited/Setting/List/Page", "GET"),
	CASH_COUPON_ORDER_LIMITED_SETTING_EDIT_PAGE("现金券限额配置-编辑页面", "AUTH_CASH_COUPON_ORDER_LIMITED_SETTING_EDIT_PAGE", "/Coupon/Cash/OrderLimited/Setting/Edit/Page", "GET"),
	CASH_COUPON_ORDER_LIMITED_SETTING_LIST("现金券限额配置-列表请求", "AUTH_CASH_COUPON_ORDER_LIMITED_SETTING_LIST", "/coupon/cash/orderLimited/setting/list", "POST"),
	CASH_COUPON_ORDER_LIMITED_SETTING_LOAD("现金券限额配置-加载请求", "AUTH_CASH_COUPON_ORDER_LIMITED_SETTING_LOAD", "/coupon/cash/orderLimited/setting/load", "GET"),
	CASH_COUPON_ORDER_LIMITED_SETTING_SAVE("现金券限额配置-保存请求", "AUTH_CASH_COUPON_ORDER_LIMITED_SETTING_SAVE", "/coupon/cash/orderLimited/setting/save", "POST"),
	CASH_COUPON_ORDER_LIMITED_SETTING_DELETE("现金券限额配置-删除请求", "AUTH_CASH_COUPON_ORDER_LIMITED_SETTING_DELETE", "/coupon/cash/orderLimited/setting/delete", "POST"),
	
	CLIENT_USER_SUBLIST_PAGE("下级会员-列表页面", "AUTH_CLIENT_USER_SUBLIST_PAGE", "/Client/User/SubList/Page", "GET"),
	
	CLIENT_POINTS_LIST_PAGE("会员积分-列表页面", "AUTH_CLIENT_POINTS_LIST_PAGE", "/Client/Points/List/Page", "GET"),
	CLIENT_POINTS_LIST("会员积分-列表请求", "AUTH_CLIENT_POINTS_LIST", "/client/points/list", "POST"),
	CLIENT_POINTS_JOURNAL_LIST_PAGE("积分明细-列表页面", "AUTH_CLIENT_POINTS_JOURNAL_LIST_PAGE", "/Client/Points/JOURNAL/List/Page", "GET"),
	CLIENT_POINTS_JOURNAL_LIST("积分明细-列表请求", "AUTH_CLIENT_POINTS_JOURNAL_LIST", "/client/points/journals/list", "POST"),
	CLIENT_POINTS_JOURNAL_CREATE("积分明细-新增请求", "AUTH_CLIENT_POINTS_JOURNAL_CREATE", "/client/points/journals/create", "POST"),
	
	CLIENT_LEVEL_SETTING_LIST_PAGE("会员等级-列表页面", "AUTH_CLIENT_LEVEL_SETTING_LIST_PAGE", "/Client/Level/Setting/List/Page", "GET"),
	CLIENT_LEVEL_SETTING_EDIT_PAGE("会员等级-编辑页面", "AUTH_CLIENT_LEVEL_SETTING_EDIT_PAGE", "/Client/Level/Setting/Edit/Page", "GET"),
	CLIENT_LEVEL_SETTING_LIST("会员等级-列表请求", "AUTH_CLIENT_LEVEL_SETTING_LIST", "/client/level/setting/list", "POST"),
	CLIENT_LEVEL_SETTING_LOAD("会员等级-加载请求", "AUTH_CLIENT_LEVEL_SETTING_LOAD", "/client/level/setting/load", "GET"),
	CLIENT_LEVEL_SETTING_SAVE("会员等级-保存请求", "AUTH_CLIENT_LEVEL_SETTING_SAVE", "/client/level/setting/save", "POST"),
	CLIENT_LEVEL_SETTING_CLEAR("会员等级-清除请求", "AUTH_CLIENT_LEVEL_SETTING_CLEAR", "/client/level/setting/clear", "GET"),
	
	CLIENT_ACTIVITY_EVENT_LIST_PAGE("活动日配置-列表页面", "AUTH_CLIENT_ACTIVITY_EVENT_LIST_PAGE", "/Client/ActivityEvent/List/Page", "GET"),
	CLIENT_ACTIVITY_EVENT_EDIT_PAGE("活动日配置-编辑页面", "AUTH_CLIENT_ACTIVITY_EVENT_EDIT_PAGE", "/Client/ActivityEvent/Edit/Page", "GET"),
	CLIENT_ACTIVITY_EVENT_LIST("活动日配置-列表请求", "AUTH_CLIENT_ACTIVITY_EVENT_LIST", "/client/activityEvent/list", "POST"),
	CLIENT_ACTIVITY_EVENT_LOAD("活动日配置-加载请求", "AUTH_CLIENT_ACTIVITY_EVENT_LOAD", "/client/activityEvent/load", "GET"),
	CLIENT_ACTIVITY_EVENT_SAVE("活动日配置-保存请求", "AUTH_CLIENT_ACTIVITY_EVENT_SAVE", "/client/activityEvent/save", "POST"),
	CLIENT_ACTIVITY_EVENT_TRIGGER("活动日配置-中止/重启请求", "AUTH_CLIENT_ACTIVITY_EVENT_TRIGGER", "/client/activityEvent/trigger", "GET"),
	CLIENT_ACTIVITY_EVENT_DELETE("活动日配置-删除请求", "AUTH_CLIENT_ACTIVITY_EVENT_CLEAR", "/client/activityEvent/clear", "POST"),
	
	SITE_SETTING_PAGE("站点配置-配置页面", "AUTH_SITE_SETTING_PAGE", "/Setting/Site/Page", "GET"),
	SITE_SETTING_LOAD("站点配置-加载请求", "AUTH_SITE_SETTING_LOAD", "/setting/site/load", "GET"),
	SITE_SETTING_SAVE("站点配置-保存请求", "AUTH_SITE_SETTING_SAVE", "/setting/site/save", "POST"),
	SITE_SETTING_UPLOAD("站点配置-上传请求", "AUTH_SITE_SETTING_UPLOAD", "/setting/site/upload", "POST"),
	
	ADVERTISEMENT_SETTING_LIST_PAGE("广告位配置-列表页面", "AUTH_ADVERTISEMENT_SETTING_LIST_PAGE", "/Advertisement/Setting/List/Page", "GET"),
	ADVERTISEMENT_SETTING_EDIT_PAGE("广告位配置-编辑页面", "AUTH_ADVERTISEMENT_SETTING_EDIT_PAGE", "/Advertisement/Setting/Edit/Page", "GET"),
	ADVERTISEMENT_SETTING_LIST("广告位配置-列表请求", "AUTH_ADVERTISEMENT_SETTING_LIST", "/advertisement/list", "POST"),
	ADVERTISEMENT_SETTING_LOAD("广告位配置-加载请求", "AUTH_ADVERTISEMENT_SETTING_LOAD", "/advertisement/load", "GET"),
	ADVERTISEMENT_SETTING_FILE_UPLOAD("广告位配置-上传请求", "AUTH_ADVERTISEMENT_SETTING_FILE_UPLOAD", "/advertisement/file/upload", "POST"),
	ADVERTISEMENT_SETTING_SAVE("广告位配置-保存请求", "AUTH_ADVERTISEMENT_SETTING_SAVE", "/advertisement/save", "POST"),
	ADVERTISEMENT_SETTING_DELETE("广告位配置-删除请求", "AUTH_ADVERTISEMENT_SETTING_DELETE", "/advertisement/delete", "POST"),
	ADVERTISEMENT_SETTING_FILE_LOAD("广告位配置-文件请求", "AUTH_ADVERTISEMENT_SETTING_FILE_LOAD", "/advertisement/file/load", "GET"),
	
	ARTICLE_CATEGORY_LIST_PAGE("文章分类-列表页面", "AUTH_ARTICLE_CATEGORY_LIST_PAGE", "/Article/Category/List/Page", "GET"),
	ARTICLE_CATEGORY_EDIT_PAGE("文章分类-编辑页面", "AUTH_ARTICLE_CATEGORY_EDIT_PAGE", "/Article/Category/Edit/Page", "GET"),
	ARTICLE_CATEGORY_LIST("文章分类-列表请求", "AUTH_ARTICLE_CATEGORY_LIST", "/article/category/list", "POST"),
	ARTICLE_CATEGORY_LIST_ALL("文章分类-所有分类请求", "AUTH_ARTICLE_CATEGORY_LIST_ALL", "/article/category/list/all", "GET"),
	ARTICLE_CATEGORY_LOAD("文章分类-加载请求", "AUTH_ARTICLE_CATEGORY_LOAD", "/article/category/load", "GET"),
	ARTICLE_CATEGORY_SAVE("文章分类-保存请求", "AUTH_ARTICLE_CATEGORY_SAVE", "/article/category/save", "POST"),
	ARTICLE_CATEGORY_DELETE("文章分类-删除请求", "AUTH_ARTICLE_CATEGORY_DELETE", "/article/category/delete", "POST"),
	
	ARTICLE_LIST_PAGE("文章管理-列表页面", "AUTH_ARTICLE_LIST_PAGE", "/Article/List/Page", "GET"),
	ARTICLE_EDIT_PAGE("文章管理-编辑页面", "AUTH_ARTICLE_EDIT_PAGE", "/Article/Edit/Page", "GET"),
	ARTICLE_LIST("文章管理-列表请求", "AUTH_ARTICLE_LIST", "/article/list", "POST"),
	ARTICLE_LOAD("文章管理-加载请求", "AUTH_ARTICLE_LOAD", "/article/load", "GET"),
	ARTICLE_FILE_UPLOAD("文章管理-上传请求", "AUTH_ARTICLE_FILE_UPLOAD", "/article/file/upload", "POST"),
	ARTICLE_SAVE("文章管理-保存请求", "AUTH_ARTICLE_SAVE", "/article/save", "POST"),
	ARTICLE_DELETE("文章管理-删除请求", "AUTH_ARTICLE_DELETE", "/article/delete", "POST"),
	ARTICLE_FILE_LOAD("文章管理-文件请求", "AUTH_ARTICLE_FILE_LOAD", "/article/file/load", "GET"),
	ARTICLE_CONTENT_IMAGE_UPLOAD("文章管理-内容图片上传请求", "AUTH_ARTICLE_CONTENT_IMAGE_UPLOAD", "/article/content/image/upload", "POST"),
	;
	
	private String name;
	private String code;
	private String link;
	private String method;
	private boolean init;

	private AuthorityDefinitions(String name, String code, String link, String method) {
		this(name, code, link, method, false);
	}
	
	private AuthorityDefinitions(String name, String code, String link, String method, boolean init) {
		this.name = name;
		this.code = code;
		this.link = link;
		this.method = method;
		this.init = init;
	}
	
	public boolean isInit() {
		return init;
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
