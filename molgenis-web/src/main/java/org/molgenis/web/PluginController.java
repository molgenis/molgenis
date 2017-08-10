package org.molgenis.web;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.plugin.Plugin;
import org.molgenis.data.plugin.PluginFactory;
import org.molgenis.data.plugin.PluginRegistry;
import org.molgenis.data.settings.DefaultSettingsEntityType;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * Abstract base class for all MOLGENIS plugin controllers
 */
public abstract class PluginController
{
	public static final String PLUGIN_URI_PREFIX = "/plugin/";

	@Autowired
	private DataService dataService;

	@Autowired
	private PluginRegistry molgenisPluginRegistry;

	/**
	 * Base URI for a plugin
	 */
	private final String uri;

	private final PluginFactory molgenisPluginFactory;

	public PluginController(String uri)
	{
		this(uri, null);
	}

	public PluginController(String uri, PluginFactory molgenisPluginFactory)
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
	 */
	public String getUri()
	{
		return uri;
	}

	/**
	 * Returns the unique id of the plugin
	 */
	public String getId()
	{
		return uri.substring(PLUGIN_URI_PREFIX.length());
	}

	/**
	 * Returns an entity containing settings for a plugin or null if no settings exist.
	 *
	 * @return entity or null
	 */
	public Entity getPluginSettings()
	{
		String entityTypeId = DefaultSettingsEntityType.getSettingsEntityName(getId());
		return RunAsSystemAspect.runAsSystem(() -> getPluginSettings(entityTypeId));
	}

	private Entity getPluginSettings(String entityTypeId)
	{
		return dataService.hasRepository(entityTypeId) ? dataService.findOneById(entityTypeId, getId()) : null;
	}

	@PostConstruct
	private void registerPlugin()
	{
		molgenisPluginRegistry.registerPlugin(new Plugin(getId(), getId(), "", "")); // FIXME
		if (molgenisPluginFactory != null)
		{
			molgenisPluginRegistry.registerPluginFactory(molgenisPluginFactory);
		}
	}

	/**
	 * Testability
	 */
	void setDataService(DataService dataService)
	{
		this.dataService = dataService;
	}
}
