package org.molgenis.ui.menu;

import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.ui.MolgenisUiMenuItem;
import org.molgenis.ui.MolgenisUiMenuItemType;

public class MenuItemToMolgenisUiMenuItemAdapter implements MolgenisUiMenuItem
{
	private final MenuItem menuItem;

	public MenuItemToMolgenisUiMenuItemAdapter(MenuItem menuItem)
	{
		if (menuItem == null) throw new IllegalArgumentException("menuItem is null");
		this.menuItem = menuItem;
	}

	@Override
	public String getId()
	{
		return menuItem.getId();
	}

	@Override
	public String getName()
	{
		return menuItem.getLabel();
	}

	@Override
	public String getUrl()
	{
		String params = menuItem.getParams();
		return params != null && !params.isEmpty() ? menuItem.getId() + '?' + params : menuItem.getId();
	}

	@Override
	public MolgenisUiMenuItemType getType()
	{
		switch (menuItem.getType())
		{
			case MENU:
				return MolgenisUiMenuItemType.MENU;
			case PLUGIN:
				return MolgenisUiMenuItemType.PLUGIN;
			default:
				throw new RuntimeException("Unknown MolgenisUiMenuItemType [" + menuItem.getType() + "]");
		}
	}

	@Override
	public MolgenisUiMenu getParentMenu()
	{
		throw new UnsupportedOperationException();
	}
}
