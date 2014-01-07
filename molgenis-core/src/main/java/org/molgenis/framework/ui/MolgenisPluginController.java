package org.molgenis.framework.ui;

/**
 * Abstract base class for all MOLGENIS plugin controllers
 */
public abstract class MolgenisPluginController
{
	public static final String PLUGIN_URI_PREFIX = "/plugin/";

	/** Base URI for a plugin */
	private final String uri;

	public MolgenisPluginController(String uri)
	{
		if (uri == null) throw new IllegalArgumentException("uri is null");
		if (!uri.startsWith(PLUGIN_URI_PREFIX))
		{
			throw new IllegalArgumentException("uri does not start with " + PLUGIN_URI_PREFIX);
		}
		this.uri = uri;
	}

	/**
	 * Returns the base URI of the plugin
	 * 
	 * @return
	 */
	public String getUri()
	{
		return uri;
	}

	/** Returns the unique id of the plugin */
	public String getId()
	{
		return uri.substring(PLUGIN_URI_PREFIX.length());
	}
}
