package org.molgenis.migrate.version.v1_9;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_COUNT_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX;

import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.migrate.version.MolgenisVersionService;
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
	private static final Logger LOG = LoggerFactory.getLogger(AddPluginSettingsPermissionsMigrator.class);

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
		if (molgenisVersionService.getMolgenisVersionFromServerProperties() == 15)
		{
			LOG.info("Creating UserAuthority and GroupAuthority instances for plugin settings entities ...");
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
						String settingsRole = AUTHORITY_ENTITY_READ_PREFIX + "SETTINGS_" + pluginId;
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
			for (GroupAuthority groupAuthority : dataService.findAll(GroupAuthority.ENTITY_NAME, GroupAuthority.class))
			{
				String groupRole = groupAuthority.getRole();

				if (groupRole.startsWith(AUTHORITY_PLUGIN_PREFIX))
				{
					String pluginId;
					if (groupRole.startsWith(AUTHORITY_PLUGIN_COUNT_PREFIX))
					{
						pluginId = groupRole.substring(AUTHORITY_PLUGIN_COUNT_PREFIX.length());
					}
					else if (groupRole.startsWith(AUTHORITY_PLUGIN_READ_PREFIX))
					{
						pluginId = groupRole.substring(AUTHORITY_PLUGIN_READ_PREFIX.length());
					}
					else if (groupRole.startsWith(AUTHORITY_PLUGIN_WRITE_PREFIX))
					{
						pluginId = groupRole.substring(AUTHORITY_PLUGIN_WRITE_PREFIX.length());
					}
					else
					{
						LOG.warn("Authority contains unknown permission [" + groupRole + "].");
						pluginId = null;
					}

					if (pluginId != null)
					{
						MolgenisGroup molgenisGroup = groupAuthority.getMolgenisGroup();
						String settingsRole = AUTHORITY_ENTITY_READ_PREFIX + "SETTINGS_" + pluginId;
						if (dataService.count(UserAuthority.ENTITY_NAME, new QueryImpl()
								.eq(GroupAuthority.ROLE, settingsRole).and().eq(MolgenisGroup.ID, molgenisGroup)) == 0)
						{
							GroupAuthority settingsGroupAuthority = new GroupAuthority();
							settingsGroupAuthority.setRole(settingsRole);
							settingsGroupAuthority.setMolgenisGroup(molgenisGroup);
							dataService.add(GroupAuthority.ENTITY_NAME, settingsGroupAuthority);
						}
					}
				}

			}
			LOG.info("Created UserAuthority and GroupAuthority instances for plugin settings entities");
		}
		return this;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
	{
		RunAsSystemProxy.runAsSystem(() -> migrateSettings());
	}
}
