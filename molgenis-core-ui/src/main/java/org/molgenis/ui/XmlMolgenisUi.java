package org.molgenis.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.molgenis.security.core.MolgenisPermissionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @deprecated use {@link org.molgenis.ui.menu.MenuMolgenisUi} instead
 */
@Deprecated
public class XmlMolgenisUi implements MolgenisUi
{
	private final Molgenis molgenisUi;
	private final MolgenisPermissionService molgenisPermissionService;

	@Autowired
	public XmlMolgenisUi(XmlMolgenisUiLoader xmlMolgenisUiLoader, MolgenisPermissionService molgenisPermissionService)
			throws IOException
	{
		this.molgenisUi = checkNotNull(xmlMolgenisUiLoader).load();
		this.molgenisPermissionService = checkNotNull(molgenisPermissionService);
	}

	@Override
	public MolgenisUiMenu getMenu()
	{
		return new XmlMolgenisUiMenu(molgenisUi.getMenu(), molgenisPermissionService);
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
