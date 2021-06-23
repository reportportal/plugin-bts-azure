package com.epam.reportportal.extension.example.command.role;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public abstract class AdminCommand<T> extends AbstractRoleBasedCommand<T> {

	@Override
	public void validateRole(Map<String, Object> params) {
		ReportPortalUser user = (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		BusinessRule.expect(UserRole.ADMINISTRATOR.equals(user.getUserRole()), Predicate.isEqual(true))
				.verify(ErrorType.ACCESS_DENIED, "Only user with role 'ADMINISTRATOR' is allowed to execute command.");
	}

}
