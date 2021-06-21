package com.epam.reportportal.extension.example.command.role;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.ta.reportportal.entity.integration.Integration;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractRoleBasedCommand<T> implements PluginCommand<T> {

	protected abstract void validateRole(Map<String, Object> params);

	protected abstract T invokeCommand(Integration integration, Map<String, Object> params);

	@Override
	public T executeCommand(Integration integration, Map<String, Object> params) {
		validateRole(params);
		return invokeCommand(integration, params);
	}

}
