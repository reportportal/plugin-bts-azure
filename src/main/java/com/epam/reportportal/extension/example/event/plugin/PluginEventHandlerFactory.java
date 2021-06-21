package com.epam.reportportal.extension.example.event.plugin;

import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.reportportal.extension.example.event.EventHandlerFactory;
import com.epam.reportportal.extension.example.event.handler.EventHandler;
import com.epam.reportportal.extension.example.event.handler.plugin.PluginLoadedEventHandler;
import com.epam.reportportal.extension.example.info.PluginInfoProvider;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginEventHandlerFactory implements EventHandlerFactory<PluginEvent> {

	public static final String LOAD_KEY = "load";

	private final Map<String, EventHandler<PluginEvent>> eventHandlerMapping;

	public PluginEventHandlerFactory(IntegrationTypeRepository integrationTypeRepository, IntegrationRepository integrationRepository,
			PluginInfoProvider pluginInfoProvider) {
		this.eventHandlerMapping = new HashMap<>();
		this.eventHandlerMapping.put(LOAD_KEY,
				new PluginLoadedEventHandler(integrationTypeRepository, integrationRepository, pluginInfoProvider)
		);
	}

	@Override
	public EventHandler<PluginEvent> getEventHandler(String key) {
		return eventHandlerMapping.get(key);
	}
}
