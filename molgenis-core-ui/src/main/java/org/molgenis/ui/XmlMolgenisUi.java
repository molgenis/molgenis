package org.molgenis.ui;

import org.molgenis.security.core.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * @deprecated use {@link org.molgenis.ui.menu.MenuMolgenisUi} instead
 */
@Deprecated
public class XmlMolgenisUi implements MolgenisUi
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
	public MolgenisUiMenu getMenu()
	{
		return new XmlMolgenisUiMenu(molgenisUi.getMenu(), permissionService);
	}

	@Override
	public MolgenisUiMenu getMenu(String menuId)
	{
		return getMenuRecursive(getMenu(), menuId);
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
