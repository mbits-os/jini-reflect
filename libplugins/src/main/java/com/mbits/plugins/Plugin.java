package com.mbits.plugins;

/**
 * Base class for the plugins managed by the Plugin Engine implementation. The only operation this plugin requires, is the name of the plugin.
 */
public interface Plugin {
	/**
	 * Name of the loaded plugin.
	 * @return name of the plugin
	 */
	String getName();
}
