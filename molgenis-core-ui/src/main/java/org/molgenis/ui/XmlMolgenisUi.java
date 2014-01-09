package org.molgenis.ui;

import java.io.IOException;

import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;

public class XmlMolgenisUi implements MolgenisUi
{
	static final String DEFAULT_APP_HREF_LOGO = "/img/logo_molgenis_small.png";
	static final String KEY_APP_NAME = "app.name";
	static final String KEY_APP_HREF_LOGO = "app.href.logo";
	static final String KEY_APP_HREF_CSS = "app.href.css";

	private final Molgenis molgenisUi;
	private final MolgenisSettings molgenisSettings;
	private final MolgenisPermissionService molgenisPermissionService;

	@Autowired
	public XmlMolgenisUi(XmlMolgenisUiLoader xmlMolgenisUiLoader, MolgenisSettings molgenisSettings,
			MolgenisPermissionService molgenisPermissionService) throws IOException
	{
		if (xmlMolgenisUiLoader == null) throw new IllegalArgumentException("XmlMolgenisUiLoader is null");
		if (molgenisSettings == null) throw new IllegalArgumentException("MolgenisSettings is null");
		if (molgenisPermissionService == null) throw new IllegalArgumentException("MolgenisPermissionService is null");
		this.molgenisUi = xmlMolgenisUiLoader.load();
		this.molgenisSettings = molgenisSettings;
		this.molgenisPermissionService = molgenisPermissionService;
	}

	@Override
	public String getTitle()
	{
		String title = molgenisSettings.getProperty(KEY_APP_NAME);
		if (title == null) title = molgenisUi.getLabel();
		if (title == null) title = molgenisUi.getName();
		return title;
	}

	@Override
	public String getHrefLogo()
	{
		return molgenisSettings.getProperty(KEY_APP_HREF_LOGO, DEFAULT_APP_HREF_LOGO);
	}

	@Override
	public String getHrefCss()
	{
		return molgenisSettings.getProperty(KEY_APP_HREF_CSS);
	}

	@Override
	public MolgenisUiMenu getMenu()
	{
		return new XmlMolgenisUiMenu(molgenisUi.getMenu(), molgenisPermissionService);
	}

	@Override
	public MolgenisUiMenu getMenu(String menuId)
	{
		MolgenisUiMenu menu = getMenuRecursive(getMenu(), menuId);
		if (menu == null) throw new RuntimeException("unknown menu or menu with no (accessible) items: " + menuId);
		return menu;
	}

	private MolgenisUiMenu getMenuRecursive(MolgenisUiMenu menu, String menuId)
	{
		if (menu.getId().equals(menuId)) return menu;

		for (MolgenisUiMenuItem menuItem : menu.getItems())
		{
			if (menuItem.getType() == MolgenisUiMenuItemType.MENU)
			{
				MolgenisUiMenu requestedMenu = getMenuRecursive((MolgenisUiMenu) menuItem, menuId);
				if (requestedMenu != null) return requestedMenu;
			}
		}
		return null;
	}
}
