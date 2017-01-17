package org.molgenis.ui;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.settings.DefaultSettingsEntityType;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginFactory;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Map;

import static com.google.api.client.util.Maps.newHashMap;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

/**
 * Abstract base class for all MOLGENIS plugin controllers
 */
public abstract class MolgenisPluginController
{
	public static final String PLUGIN_URI_PREFIX = "/plugin/";

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisPluginRegistry molgenisPluginRegistry;

	/**
	 * Base URI for a plugin
	 */
	private final String uri;

	private Map<String, String> requiredSettingEntities = newHashMap();

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

	/**
	 * Set a {@link Map} of entity name and permission key value pairs.
	 * <p>
	 * This allows every plugin to register which entities should be allowed access to when a user
	 * is granted permission on a plugin
	 */
	public void setRequiredSettingEntities(Map<String, String> requiredSettingEntities)
	{
		this.requiredSettingEntities = requiredSettingEntities;
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
		String entityName = DefaultSettingsEntityType.getSettingsEntityName(getId());
		return runAsSystem(() -> getPluginSettings(entityName));
	}

	private Entity getPluginSettings(String entityName)
	{
		return dataService.hasRepository(entityName) ? dataService.findOneById(entityName, getId()) : null;
	}

	@PostConstruct
	private void registerPlugin()
	{
		molgenisPluginRegistry
				.registerPlugin(new MolgenisPlugin(getId(), getId(), "", "", requiredSettingEntities)); // FIXME
		if (molgenisPluginFactory != null)
		{
			molgenisPluginRegistry.registerPluginFactory(molgenisPluginFactory);
		}
	}

	/**
	 * Testability
	 *
	 * @param dataService
	 */
	void setDataService(DataService dataService)
	{
		this.dataService = dataService;
	}
}