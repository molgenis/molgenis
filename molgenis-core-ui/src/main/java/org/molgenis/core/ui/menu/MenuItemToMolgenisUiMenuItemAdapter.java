package org.molgenis.core.ui.menu;

import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.web.UiMenu;
import org.molgenis.web.UiMenuItem;
import org.molgenis.web.UiMenuItemType;

public class MenuItemToMolgenisUiMenuItemAdapter implements UiMenuItem
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
	public UiMenuItemType getType()
	{
		switch (menuItem.getType())
		{
			case MENU:
				return UiMenuItemType.MENU;
			case PLUGIN:
				return UiMenuItemType.PLUGIN;
			default:
				throw new UnexpectedEnumException(menuItem.getType());
		}
	}

	@Override
	public UiMenu getParentMenu()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAuthorized()
	{
		throw new UnsupportedOperationException();
	}
}
