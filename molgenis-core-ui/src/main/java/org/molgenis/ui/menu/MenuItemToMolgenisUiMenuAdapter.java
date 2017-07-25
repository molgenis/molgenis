package org.molgenis.ui.menu;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.ui.MolgenisUiMenuItem;

import java.util.*;

public class MenuItemToMolgenisUiMenuAdapter extends MenuItemToMolgenisUiMenuItemAdapter implements MolgenisUiMenu
{
	private final MenuItem menu;
	private final MenuItem rootMenu;

	public MenuItemToMolgenisUiMenuAdapter(MenuItem menu, MenuItem rootMenu)
	{
		super(menu);
		if (menu == null) throw new IllegalArgumentException("menu is null");
		if (rootMenu == null) throw new IllegalArgumentException("rootMenu is null");
		this.menu = menu;
		this.rootMenu = rootMenu;
	}

	@Override
	public List<MolgenisUiMenuItem> getItems()
	{
		List<MenuItem> items = menu.getItems();
		return items != null ? Lists.newArrayList(
				Iterables.transform(items, (Function<MenuItem, MolgenisUiMenuItem>) menuItem ->
				{
					if (menuItem.getType() == MenuItemType.PLUGIN)
						return new MenuItemToMolgenisUiMenuItemAdapter(menuItem);
					else return new MenuItemToMolgenisUiMenuAdapter(menuItem, rootMenu);
				})) : Collections.emptyList();
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
		if (menu.equals(rootMenu)) return Collections.singletonList(this);

		Map<String, MenuItem> menuParentMap = new HashMap<>();
		createMenuParentMapRec(rootMenu, null, menu, menuParentMap);

		List<MolgenisUiMenu> breadcrumb = new ArrayList<>();
		MenuItem currentMenu = menu;
		while (currentMenu != null)
		{
			breadcrumb.add(new MenuItemToMolgenisUiMenuAdapter(currentMenu, rootMenu));
			currentMenu = menuParentMap.get(currentMenu.getId());
		}
		return Lists.reverse(breadcrumb);
	}

	private void createMenuParentMapRec(MenuItem menu, MenuItem parentMenu, MenuItem stopMenu,
			Map<String, MenuItem> breadcrumbMap)
	{
		breadcrumbMap.put(menu.getId(), parentMenu);
		if (menu.getId().equals(stopMenu.getId())) return;

		for (MenuItem menuItem : menu.getItems())
		{
			if (menuItem.getType() == MenuItemType.MENU)
			{
				createMenuParentMapRec(menuItem, menu, stopMenu, breadcrumbMap);
			}
		}
	}
}
