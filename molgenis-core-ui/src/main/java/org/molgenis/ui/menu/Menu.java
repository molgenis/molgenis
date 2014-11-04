package org.molgenis.ui.menu;

public class Menu extends MenuItem
{
	public MenuItem findMenuItem(String id)
	{
		return MenuUtils.findMenuItem(id, getItems());
	}
}
