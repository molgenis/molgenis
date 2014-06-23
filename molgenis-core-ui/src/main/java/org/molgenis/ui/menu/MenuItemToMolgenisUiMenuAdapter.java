package org.molgenis.ui.menu;

import java.util.List;

import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.ui.MolgenisUiMenuItem;
import org.molgenis.ui.MolgenisUiMenuItemType;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MenuItemToMolgenisUiMenuAdapter implements MolgenisUiMenu
{
	private final MenuItem menuItem;
	private final MolgenisPermissionService molgenisPermissionService;

	public MenuItemToMolgenisUiMenuAdapter(MenuItem menuItem, MolgenisPermissionService molgenisPermissionService)
	{
		if (menuItem == null) throw new IllegalArgumentException("menuItem is null");
		if (molgenisPermissionService == null) throw new IllegalArgumentException("molgenisPermissionService is null");
		this.menuItem = menuItem;
		this.molgenisPermissionService = molgenisPermissionService;
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
		return menuItem.getId();
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

	@Override
	public boolean isAuthorized()
	{
		return isAuthorizedRec(menuItem);
	}

	private boolean isAuthorizedRec(MenuItem menuItem)
	{
		switch (menuItem.getType())
		{
			case MENU:
				for (MenuItem submenuItem : menuItem.getItems())
				{
					if (isAuthorizedRec(submenuItem)) return true;
				}
				return false;
			case PLUGIN:
				return molgenisPermissionService.hasPermissionOnPlugin(menuItem.getId(), Permission.COUNT);
			default:
				throw new RuntimeException("Unknown MolgenisUiMenuItemType [" + menuItem.getType() + "]");
		}
	}

	@Override
	public List<MolgenisUiMenuItem> getItems()
	{
		return Lists.newArrayList(Iterables.transform(menuItem.getItems(), new Function<MenuItem, MolgenisUiMenuItem>()
		{
			@Override
			public MolgenisUiMenuItem apply(MenuItem input)
			{
				return new MenuItemToMolgenisUiMenuAdapter(input, molgenisPermissionService);
			}
		}));
	}

	@Override
	public boolean containsItem(String itemId)
	{
		List<MenuItem> items = menuItem.getItems();
		if (items != null)
		{
			for (MenuItem submenuItem : items)
			{
				if (submenuItem.getId().equals(itemId)) return true;
			}
		}
		return false;
	}

	@Override
	public MolgenisUiMenuItem getActiveItem()
	{
		List<MenuItem> items = menuItem.getItems();
		if (items != null)
		{
			for (MenuItem submenuItem : items)
			{
				if (isAuthorizedRec(submenuItem))
				{
					return new MenuItemToMolgenisUiMenuAdapter(submenuItem, molgenisPermissionService);
				}
			}
		}
		return null;
	}

	@Override
	public List<MolgenisUiMenu> getBreadcrumb()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
