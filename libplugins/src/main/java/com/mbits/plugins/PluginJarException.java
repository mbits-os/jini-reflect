package com.mbits.plugins;

import java.io.IOException;

public class PluginJarException extends IOException {

	private static final long serialVersionUID = -5796810570566068244L;

	public PluginJarException() {
	}

	public PluginJarException(String message) {
		super(message);
	}

	public PluginJarException(Throwable cause) {
		super(cause);
	}

	public PluginJarException(String message, Throwable cause) {
		super(message, cause);
	}

}
