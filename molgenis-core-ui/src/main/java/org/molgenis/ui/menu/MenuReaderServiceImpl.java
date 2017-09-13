package org.molgenis.ui.menu;

import com.google.gson.GsonBuilder;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.runas.RunAsSystem;

import static java.util.Objects.requireNonNull;

public class MenuReaderServiceImpl implements MenuReaderService
{
	private final AppSettings appSettings;

	public MenuReaderServiceImpl(AppSettings appSettings)
	{
		this.appSettings = requireNonNull(appSettings);
	}

	@Override
	@RunAsSystem
	public Menu getMenu()
	{
		String menuJson = appSettings.getMenu();
		return menuJson != null ? new GsonBuilder().create().fromJson(menuJson, Menu.class) : null;
	}
}
