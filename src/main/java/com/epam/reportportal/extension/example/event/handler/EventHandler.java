package com.epam.reportportal.extension.example.event.handler;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface EventHandler<T> {

	void handle(T event);
}
