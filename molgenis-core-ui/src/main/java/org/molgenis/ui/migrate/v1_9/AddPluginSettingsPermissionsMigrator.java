package org.molgenis.ui.migrate.v1_9;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_COUNT_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.version.MolgenisVersionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class AddPluginSettingsPermissionsMigrator implements ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(RuntimePropertyToStaticContentMigrator.class);

	private final DataService dataService;
	private final MolgenisVersionService molgenisVersionService;

	@Autowired
	public AddPluginSettingsPermissionsMigrator(DataService dataService, MolgenisVersionService molgenisVersionService)
	{
		this.dataService = checkNotNull(dataService);
		this.molgenisVersionService = checkNotNull(molgenisVersionService);
	}

	private AddPluginSettingsPermissionsMigrator migrateSettings()
	{
		if (molgenisVersionService.getMolgenisVersionFromServerProperties() == 13)
		{
			LOG.info("Adding Settings plugin to Admin menu ...");
			for (UserAuthority userAuthority : dataService.findAll(UserAuthority.ENTITY_NAME, UserAuthority.class))
			{
				String role = userAuthority.getRole();

				if (role.startsWith(AUTHORITY_PLUGIN_PREFIX))
				{
					String pluginId;
					if (role.startsWith(AUTHORITY_PLUGIN_COUNT_PREFIX))
					{
						pluginId = role.substring(AUTHORITY_PLUGIN_COUNT_PREFIX.length());
					}
					else if (role.startsWith(AUTHORITY_PLUGIN_READ_PREFIX))
					{
						pluginId = role.substring(AUTHORITY_PLUGIN_READ_PREFIX.length());
					}
					else if (role.startsWith(AUTHORITY_PLUGIN_WRITE_PREFIX))
					{
						pluginId = role.substring(AUTHORITY_PLUGIN_WRITE_PREFIX.length());
					}
					else
					{
						LOG.warn("Authority contains unknown permission [" + role + "].");
						pluginId = null;
					}

					if (pluginId != null)
					{
						MolgenisUser molgenisUser = userAuthority.getMolgenisUser();
						String settingsRole = AUTHORITY_PLUGIN_PREFIX + "SETTINGS_" + pluginId + "_"
								+ Permission.READ.toString();
						if (dataService.count(UserAuthority.ENTITY_NAME,
								new QueryImpl().eq(UserAuthority.ROLE, settingsRole).and()
										.eq(UserAuthority.MOLGENISUSER, molgenisUser)) == 0)
						{
							UserAuthority settingsUserAuthority = new UserAuthority();
							settingsUserAuthority.setRole(settingsRole);
							settingsUserAuthority.setMolgenisUser(molgenisUser);
							dataService.add(UserAuthority.ENTITY_NAME, settingsUserAuthority);
						}
					}
				}
			}
		}
		return this;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
	{
		RunAsSystemProxy.runAsSystem(() -> migrateSettings());
	}
}
