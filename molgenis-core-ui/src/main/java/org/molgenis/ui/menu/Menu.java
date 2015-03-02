package org.molgenis.ui.menu;

import java.util.List;

public class Menu extends MenuItem
{
	public MenuItem findMenuItem(String id)
	{
		return MenuUtils.findMenuItem(id, getItems());
	}

	public List<MenuItem> deleteMenuItem(String id)
	{
		return MenuUtils.deleteMenuItem(id, getItems());
	}
}
