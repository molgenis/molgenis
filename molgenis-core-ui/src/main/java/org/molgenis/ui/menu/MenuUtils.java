package org.molgenis.ui.menu;

import java.util.List;

import org.elasticsearch.common.collect.Lists;

public class MenuUtils
{
	public static MenuItem findMenuItem(String id, List<MenuItem> menu)
	{
		for (MenuItem item : menu)
		{
			if (item.getId().equals(id))
			{
				return item;
			}

			if (item.getItems() != null)
			{
				MenuItem found = findMenuItem(id, item.getItems());
				if (found != null)
				{
					return found;
				}

			}
		}

		return null;
	}

	public static List<MenuItem> deleteMenuItem(String id, List<MenuItem> menu)
	{
		return recursivelyDeleteMenuItem(id, menu, Lists.newArrayList());
	}

	private static List<MenuItem> recursivelyDeleteMenuItem(String id, List<MenuItem> menu, List<MenuItem> newMenu)
	{
		for (MenuItem item : menu)
		{
			if (!item.getId().equals(id))
			{
				newMenu.add(item);
			}

			if (item.getItems() != null)
			{
				item.setItems(recursivelyDeleteMenuItem(id, item.getItems(), Lists.newArrayList()));
			}
		}
		return newMenu;
	}
}
