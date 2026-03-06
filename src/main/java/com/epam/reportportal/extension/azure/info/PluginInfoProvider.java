package com.epam.reportportal.extension.azure.info;

import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PluginInfoProvider {

	IntegrationType provide(IntegrationType integrationType);
}
