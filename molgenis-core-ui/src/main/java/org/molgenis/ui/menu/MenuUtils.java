package org.molgenis.ui.menu;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Stack;

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

	/**
	 * Return URI path to menu item of the given id or null if item does not exist.
	 *
	 * @param id
	 * @param menu
	 * @return
	 */
	public static String findMenuItemPath(String id, Menu menu)
	{
		Stack<MenuItem> path = new Stack<>();
		MenuItem menuItem = findMenuItemPathRec(id, menu, path);
		if (menuItem != null)
		{
			StringBuilder pathBuilder = new StringBuilder("/menu/");
			if (path.size() > 1)
			{
				pathBuilder.append(path.get(path.size() - 2).getId()).append('/');
			}
			pathBuilder.append(path.get(path.size() - 1).getId());
			return pathBuilder.toString();
		}
		else
		{
			return null;
		}

	}

	private static MenuItem findMenuItemPathRec(String id, MenuItem menu, Stack<MenuItem> path)
	{
		path.push(menu);
		for (MenuItem item : menu.getItems())
		{
			if (item.getId().equals(id))
			{
				path.push(item);
				return item;
			}
			else if (item.getType() == MenuItemType.MENU)
			{
				MenuItem itemOfInterest = findMenuItemPathRec(id, item, path);
				if (itemOfInterest != null)
				{
					return itemOfInterest;
				}
			}
		}
		path.pop();
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
