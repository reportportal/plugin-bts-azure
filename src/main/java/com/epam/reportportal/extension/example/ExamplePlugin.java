package com.epam.reportportal.extension.example;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ExamplePlugin extends Plugin {
	/**
	 * Constructor to be used by plugin manager for plugin instantiation.
	 * Your plugins have to provide constructor with this exact signature to
	 * be successfully loaded by manager.
	 *
	 * @param wrapper
	 */
	public ExamplePlugin(PluginWrapper wrapper) {
		super(wrapper);
	}
}
