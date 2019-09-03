package com.sourcecode.malls.web.filter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.sourcecode.malls.context.UserContext;
import com.sourcecode.malls.domain.merchant.MerchantShopApplication;
import com.sourcecode.malls.domain.system.User;
import com.sourcecode.malls.enums.VerificationStatus;
import com.sourcecode.malls.repository.jpa.impl.merchant.MerchantShopApplicationRepository;
import com.sourcecode.malls.util.AssertUtil;

@Component
@ConfigurationProperties(prefix = "merchant.shop.application.passed.url")
public class ApplicationCheckingFilter extends GenericFilterBean {

	@Autowired
	private MerchantShopApplicationRepository applicationRepository;

	private List<AntPathRequestMatcher> patterns;

	public void setPatterns(List<AntPathRequestMatcher> patterns) {
		this.patterns = patterns;
	}

	private User getRelatedCurrentUser() {
		User user = UserContext.get();
		if (user.getParent() != null) {
			user = user.getParent();
		}
		return user;
	}

	private void checkIfApplicationPassed() {
		Optional<MerchantShopApplication> applicationOp = applicationRepository
				.findByMerchantId(getRelatedCurrentUser().getId());
		AssertUtil.assertTrue(
				applicationOp.isPresent() && VerificationStatus.Passed.equals(applicationOp.get().getStatus()),
				"必须先通过店铺申请才能操作");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		boolean matched = false;
		for (AntPathRequestMatcher pattern : patterns) {
			matched = pattern.matches((HttpServletRequest) request);
			if (matched) {
				break;
			}
		}
		if (!matched) {
			checkIfApplicationPassed();
		}
		chain.doFilter(request, response);
	}

}
