package org.molgenis.ui.menumanager;

import com.google.gson.GsonBuilder;
import org.molgenis.data.DataService;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.MenuType;
import org.molgenis.ui.Molgenis;
import org.molgenis.ui.PluginType;
import org.molgenis.ui.XmlMolgenisUiLoader;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuItem;
import org.molgenis.ui.menu.MenuItemType;
import org.molgenis.ui.menu.MenuReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class MenuManagerServiceImpl implements MenuManagerService
{
	private static final Logger LOG = LoggerFactory.getLogger(MenuManagerServiceImpl.class);

	private final MenuReaderService menuReaderService;
	private final AppSettings appSettings;
	private final DataService dataService;

	@Autowired
	public MenuManagerServiceImpl(MenuReaderService menuReaderService, AppSettings appSettings, DataService dataService)
	{
		this.menuReaderService = requireNonNull(menuReaderService);
		this.appSettings = requireNonNull(appSettings);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_READ_menumanager')")
	@Transactional(readOnly = true)
	public Menu getMenu()
	{
		return menuReaderService.getMenu();
	}

	@Override
	@RunAsSystem
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_READ_menumanager')")
	@Transactional(readOnly = true)
	public Iterable<Plugin> getPlugins()
	{
		return dataService.findAll(PluginMetadata.PLUGIN, Plugin.class).collect(toList());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_WRITE_menumanager')")
	@Transactional
	public void saveMenu(Menu molgenisMenu)
	{
		String menuJson = new GsonBuilder().create().toJson(molgenisMenu);
		appSettings.setMenu(menuJson);
	}

	public String getDefaultMenuValue()
	{
		Molgenis molgenis;
		try
		{
			molgenis = new XmlMolgenisUiLoader().load();
		}
		catch (IOException e)
		{
			// default menu does not exist, no op
			return null;
		}

		Menu defaultMenu = loadDefaultMenu(molgenis);
		return defaultMenu != null ? new GsonBuilder().create().toJson(defaultMenu) : null;
	}

	private Menu loadDefaultMenu(Molgenis molgenis)
	{
		Menu molgenisMenu = new Menu();
		parseDefaultMenuRec(molgenisMenu, molgenis.getMenu());
		return molgenisMenu;
	}

	private void parseDefaultMenuRec(MenuItem menuItem, Object defaultMenuObj)
	{
		if (defaultMenuObj instanceof MenuType)
		{
			MenuType menuType = (MenuType) defaultMenuObj;
			menuItem.setId(menuType.getName());
			menuItem.setLabel(menuType.getLabel());
			menuItem.setType(MenuItemType.MENU);

			List<Object> defaultMenuItems = menuType.getMenuOrPlugin();
			if (defaultMenuItems != null)
			{
				List<MenuItem> items = new ArrayList<>(defaultMenuItems.size());
				for (Object defaultSubMenuObj : defaultMenuItems)
				{
					MenuItem subMenuItem = new MenuItem();
					parseDefaultMenuRec(subMenuItem, defaultSubMenuObj);
					items.add(subMenuItem);
				}
				menuItem.setItems(items);
			}
		}
		else if (defaultMenuObj instanceof PluginType)
		{
			PluginType pluginType = (PluginType) defaultMenuObj;
			menuItem.setId(pluginType.getId());
			menuItem.setLabel(pluginType.getName());
			menuItem.setType(MenuItemType.PLUGIN);

			String url = pluginType.getUrl();
			int idx;
			if ((idx = url.indexOf('?')) != -1)
			{
				String params = url.substring(idx + 1);
				menuItem.setParams(params);
			}
		}
		else
		{
			throw new RuntimeException("Unknown menu object class [" + defaultMenuObj.getClass().getName() + "]");
		}
	}
}
