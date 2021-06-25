package com.epam.reportportal.extension.azure.event;

import com.epam.reportportal.extension.azure.event.handler.EventHandler;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface EventHandlerFactory<T> {

	EventHandler<T> getEventHandler(String key);
}
