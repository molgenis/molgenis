package org.molgenis.ui.menumanager;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.ui.menu.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.GsonBuilder;

public class MenuManagerServiceImpl implements MenuManagerService
{
	private static final String KEY_MOLGENIS_MENU = "molgenis.menu";

	private final MolgenisSettings molgenisSettings;
	private final MolgenisPluginRegistry molgenisPluginRegistry;

	@Autowired
	public MenuManagerServiceImpl(MolgenisSettings molgenisSettings, MolgenisPluginRegistry molgenisPluginRegistry)
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		if (molgenisPluginRegistry == null) throw new IllegalArgumentException("molgenisPluginRegistry is null");
		this.molgenisSettings = molgenisSettings;
		this.molgenisPluginRegistry = molgenisPluginRegistry;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU, ROLE_PLUGIN_READ_MENUMANAGER')")
	@Transactional(readOnly = true)
	public Menu getMenu()
	{
		String menuJson = molgenisSettings.getProperty(KEY_MOLGENIS_MENU);
		if (menuJson == null)
		{
			throw new RuntimeException("Missing required molgenis setting [" + KEY_MOLGENIS_MENU + "]");
		}
		return new GsonBuilder().create().fromJson(menuJson, Menu.class);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU, ROLE_PLUGIN_READ_MENUMANAGER')")
	@Transactional(readOnly = true)
	public Iterable<MolgenisPlugin> getPlugins()
	{
		return molgenisPluginRegistry;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU, ROLE_PLUGIN_WRITE_MENUMANAGER')")
	@Transactional
	public void saveMenu(Menu molgenisMenu)
	{
		String menuJson = new GsonBuilder().create().toJson(molgenisMenu);
		molgenisSettings.setProperty(KEY_MOLGENIS_MENU, menuJson);
	}
}
