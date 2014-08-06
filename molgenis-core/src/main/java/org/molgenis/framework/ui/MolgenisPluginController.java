package org.molgenis.framework.ui;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract base class for all MOLGENIS plugin controllers
 */
public abstract class MolgenisPluginController
{
	public static final String PLUGIN_URI_PREFIX = "/plugin/";

	@Autowired
	private MolgenisPluginRegistry molgenisPluginRegistry;

	/** Base URI for a plugin */
	private final String uri;

	private final MolgenisPluginFactory molgenisPluginFactory;

	public MolgenisPluginController(String uri)
	{
		this(uri, null);
	}

	public MolgenisPluginController(String uri, MolgenisPluginFactory molgenisPluginFactory)
	{
		if (uri == null) throw new IllegalArgumentException("uri is null");
		if (!uri.startsWith(PLUGIN_URI_PREFIX))
		{
			throw new IllegalArgumentException("uri does not start with " + PLUGIN_URI_PREFIX);
		}
		this.uri = uri;
		this.molgenisPluginFactory = molgenisPluginFactory;
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

	@PostConstruct
	private void registerPlugin()
	{
		molgenisPluginRegistry.registerPlugin(new MolgenisPlugin(getId(), getId(), "", "")); // FIXME
		if (molgenisPluginFactory != null)
		{
			molgenisPluginRegistry.registerPluginFactory(molgenisPluginFactory);
		}
	}
}
