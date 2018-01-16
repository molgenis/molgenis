package org.molgenis.core.ui.menu;

import org.molgenis.web.UiMenuItemType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MenuItemToMolgenisUiMenuItemAdapterTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MenuItemToMolgenisUiMenuItemAdapter()
	{
		new MenuItemToMolgenisUiMenuItemAdapter(null);
	}

	@Test
	public void getId()
	{
		MenuItem menuItem = new MenuItem();
		String id = "id";
		menuItem.setId(id);
		assertEquals(new MenuItemToMolgenisUiMenuItemAdapter(menuItem).getId(), id);
	}

	@Test
	public void getName()
	{
		MenuItem menuItem = new MenuItem();
		String name = "name";
		menuItem.setLabel(name);
		assertEquals(new MenuItemToMolgenisUiMenuItemAdapter(menuItem).getName(), name);
	}

	@Test
	public void getType_menuType()
	{
		MenuItem menuItem = new MenuItem();
		menuItem.setType(MenuItemType.MENU);
		assertEquals(new MenuItemToMolgenisUiMenuItemAdapter(menuItem).getType(), UiMenuItemType.MENU);
	}

	@Test
	public void getType_pluginType()
	{
		MenuItem menuItem = new MenuItem();
		menuItem.setType(MenuItemType.PLUGIN);
		assertEquals(new MenuItemToMolgenisUiMenuItemAdapter(menuItem).getType(), UiMenuItemType.PLUGIN);
	}

	@Test
	public void getUrl_params()
	{
		MenuItem menuItem = new MenuItem();
		String id = "id";
		menuItem.setId(id);
		String params = "a=b&c=d";
		menuItem.setParams(params);
		assertEquals(new MenuItemToMolgenisUiMenuItemAdapter(menuItem).getUrl(), id + '?' + params);
	}

	@Test
	public void getUrl_noParams()
	{
		MenuItem menuItem = new MenuItem();
		String id = "id";
		menuItem.setId(id);
		assertEquals(new MenuItemToMolgenisUiMenuItemAdapter(menuItem).getUrl(), id);
	}
}
