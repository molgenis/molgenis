package org.molgenis.ui.menu;

import java.util.Collections;
import java.util.List;

import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.ui.MolgenisUiMenuItem;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MenuItemToMolgenisUiMenuAdapter extends MenuItemToMolgenisUiMenuItemAdapter implements MolgenisUiMenu
{
	private final MenuItem menu;

	public MenuItemToMolgenisUiMenuAdapter(MenuItem menu)
	{
		super(menu);
		if (menu == null) throw new IllegalArgumentException("menu is null");
		this.menu = menu;
	}

	@Override
	public List<MolgenisUiMenuItem> getItems()
	{
		List<MenuItem> items = menu.getItems();
		return items != null ? Lists.newArrayList(Iterables.transform(items,
				new Function<MenuItem, MolgenisUiMenuItem>()
				{
					@Override
					public MolgenisUiMenuItem apply(MenuItem menuItem)
					{
						if (menuItem.getType() == MenuItemType.PLUGIN) return new MenuItemToMolgenisUiMenuItemAdapter(
								menuItem);
						else return new MenuItemToMolgenisUiMenuAdapter(menuItem);
					}
				})) : Collections.<MolgenisUiMenuItem> emptyList();
	}

	@Override
	public boolean containsItem(String itemId)
	{
		List<MenuItem> items = menu.getItems();
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
		List<MenuItem> items = menu.getItems();
		if (items != null)
		{
			for (MenuItem submenuItem : items)
			{
				return new MenuItemToMolgenisUiMenuItemAdapter(submenuItem);
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
