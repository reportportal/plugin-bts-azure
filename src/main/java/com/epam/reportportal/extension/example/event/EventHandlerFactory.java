package com.epam.reportportal.extension.example.event;

import com.epam.reportportal.extension.example.event.handler.EventHandler;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface EventHandlerFactory<T> {

	EventHandler<T> getEventHandler(String key);
}
