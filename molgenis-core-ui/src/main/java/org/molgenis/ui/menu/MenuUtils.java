package org.molgenis.ui.menu;

import java.util.List;

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

}
