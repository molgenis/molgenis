package org.molgenis.ui.menu;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.GsonBuilder;

public class MenuReaderServiceImpl implements MenuReaderService
{
	public static final String KEY_MOLGENIS_MENU = "molgenis.menu";

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public MenuReaderServiceImpl(MolgenisSettings molgenisSettings)
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	@Override
	@Transactional(readOnly = true)
	@RunAsSystem
	public Menu getMenu()
	{
		String menuJson = molgenisSettings.getProperty(KEY_MOLGENIS_MENU);
		if (menuJson == null)
		{
			throw new RuntimeException("Missing required molgenis setting [" + KEY_MOLGENIS_MENU + "]");
		}
		return new GsonBuilder().create().fromJson(menuJson, Menu.class);
	}
}
