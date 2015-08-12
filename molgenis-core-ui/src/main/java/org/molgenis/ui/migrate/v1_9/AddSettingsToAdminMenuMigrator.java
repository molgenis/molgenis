package org.molgenis.ui.migrate.v1_9;

import static com.google.common.base.Preconditions.checkNotNull;

import org.molgenis.data.version.MolgenisVersionService;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuItem;
import org.molgenis.ui.menu.MenuItemType;
import org.molgenis.ui.menumanager.MenuManagerService;
import org.molgenis.ui.settingsmanager.SettingsManagerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class AddSettingsToAdminMenuMigrator implements ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(AddSettingsToAdminMenuMigrator.class);

	private final MenuManagerService menuManagerService;
	private final MolgenisVersionService molgenisVersionService;

	@Autowired
	public AddSettingsToAdminMenuMigrator(MenuManagerService menuManagerService,
			MolgenisVersionService molgenisVersionService)
	{
		this.menuManagerService = checkNotNull(menuManagerService);
		this.molgenisVersionService = checkNotNull(molgenisVersionService);
	}

	private AddSettingsToAdminMenuMigrator migrateSettings()
	{
		if (molgenisVersionService.getMolgenisVersionFromServerProperties() == 13)
		{
			LOG.info("Adding Settings plugin to Admin menu ...");
			Menu molgenisMenu = menuManagerService.getMenu();
			MenuItem adminMenu = molgenisMenu.findMenuItem("admin");
			if (adminMenu != null)
			{
				boolean hasSettingsMenuItem = false;
				for (MenuItem adminMenuItem : adminMenu.getItems())
				{
					if (adminMenuItem.getId().equals(SettingsManagerController.ID))
					{
						hasSettingsMenuItem = true;
						break;
					}
				}
				if (!hasSettingsMenuItem)
				{
					LOG.info("Added Settings plugin to Admin menu");
					adminMenu.getItems()
							.add(new MenuItem(MenuItemType.PLUGIN, SettingsManagerController.ID, "Settings"));
					menuManagerService.saveMenu(molgenisMenu);
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
