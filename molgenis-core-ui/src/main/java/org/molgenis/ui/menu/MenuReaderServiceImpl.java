package org.molgenis.ui.menu;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.GsonBuilder;

public class MenuReaderServiceImpl implements MenuReaderService
{
	private final AppSettings appSettings;

	@Autowired
	public MenuReaderServiceImpl(AppSettings appSettings)
	{
		this.appSettings = requireNonNull(appSettings);
	}

	@Override
	@Transactional(readOnly = true)
	@RunAsSystem
	public Menu getMenu()
	{
		String menuJson = appSettings.getMenu();
		return menuJson != null ? new GsonBuilder().create().fromJson(menuJson, Menu.class) : null;
	}
}
