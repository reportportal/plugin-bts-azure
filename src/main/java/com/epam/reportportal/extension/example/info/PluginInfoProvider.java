package com.epam.reportportal.extension.example.info;

import com.epam.ta.reportportal.entity.integration.IntegrationType;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PluginInfoProvider {

	IntegrationType provide(IntegrationType integrationType);
}
