package org.molgenis.ui;

import org.molgenis.data.security.PermissionService;
import org.molgenis.web.Ui;
import org.molgenis.web.UiMenu;
import org.molgenis.web.UiMenuItem;
import org.molgenis.web.UiMenuItemType;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * @deprecated use {@link org.molgenis.ui.menu.MenuMolgenisUi} instead
 */
@Deprecated
public class XmlMolgenisUi implements Ui
{
	private final Molgenis molgenisUi;
	private final PermissionService permissionService;

	@Autowired
	public XmlMolgenisUi(XmlMolgenisUiLoader xmlMolgenisUiLoader, PermissionService permissionService)
			throws IOException
	{
		this.molgenisUi = requireNonNull(xmlMolgenisUiLoader).load();
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	public UiMenu getMenu()
	{
		return new XmlMolgenisUiMenu(molgenisUi.getMenu(), permissionService);
	}

	@Override
	public String getMenuJson()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public UiMenu getMenu(String menuId)
	{
		return getMenuRecursive(getMenu(), menuId);
	}

	private UiMenu getMenuRecursive(UiMenu menu, String menuId)
	{
		if (menu.getId().equals(menuId)) return menu;

		for (UiMenuItem menuItem : menu.getItems())
		{
			if (menuItem.getType() == UiMenuItemType.MENU)
			{
				UiMenu requestedMenu = getMenuRecursive((UiMenu) menuItem, menuId);
				if (requestedMenu != null) return requestedMenu;
			}
		}
		return null;
	}
}
