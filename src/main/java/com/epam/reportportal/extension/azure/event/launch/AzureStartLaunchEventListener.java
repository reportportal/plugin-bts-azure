package com.epam.reportportal.extension.azure.event.launch;

import com.epam.reportportal.extension.event.StartLaunchEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import org.springframework.context.ApplicationListener;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class AzureStartLaunchEventListener implements ApplicationListener<StartLaunchEvent> {

	private final LaunchRepository launchRepository;

	public AzureStartLaunchEventListener(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Override
	public void onApplicationEvent(StartLaunchEvent event) {

	}
}
