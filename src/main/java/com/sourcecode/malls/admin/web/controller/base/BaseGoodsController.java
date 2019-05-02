package com.sourcecode.malls.admin.web.controller.base;

import java.util.Optional;

import com.sourcecode.malls.admin.context.UserContext;
import com.sourcecode.malls.admin.domain.merchant.MerchantShopApplication;
import com.sourcecode.malls.admin.enums.VerificationStatus;
import com.sourcecode.malls.admin.repository.jpa.impl.merchant.MerchantShopApplicationRepository;
import com.sourcecode.malls.admin.util.AssertUtil;

public interface BaseGoodsController {

	default void checkIfApplicationPassed(MerchantShopApplicationRepository applicationRepository, String type) {
		Optional<MerchantShopApplication> applicationOp = applicationRepository.findByMerchantId(UserContext.get().getId());
		AssertUtil.assertTrue(applicationOp.isPresent() && VerificationStatus.Passed.equals(applicationOp.get().getStatus()),
				"必须先通过店铺申请才能编辑商品" + type);
	}
}
