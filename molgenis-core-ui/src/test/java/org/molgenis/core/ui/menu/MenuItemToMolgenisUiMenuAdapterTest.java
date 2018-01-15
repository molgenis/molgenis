package org.molgenis.core.ui.menu;

import org.molgenis.web.UiMenu;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

public class MenuItemToMolgenisUiMenuAdapterTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MenuItemToMolgenisUiMenuAdapter()
	{
		new MenuItemToMolgenisUiMenuAdapter(null, null);
	}

	@Test
	public void containsItem_excludeSelf()
	{
		String itemId = "id";
		MenuItem menu = new MenuItem();
		menu.setId(itemId);
		assertFalse(new MenuItemToMolgenisUiMenuAdapter(menu, menu).containsItem(itemId));
	}

	@Test
	public void containsItem_child()
	{
		String itemId = "id";
		MenuItem menu = new MenuItem();
		menu.setType(MenuItemType.MENU);
		MenuItem subMenu = new MenuItem();
		subMenu.setId(itemId);
		menu.setItems(Collections.singletonList(subMenu));
		assertTrue(new MenuItemToMolgenisUiMenuAdapter(menu, menu).containsItem(itemId));
	}

	@Test
	public void getActiveItem()
	{
		String itemId = "id";
		MenuItem menu = new MenuItem();
		menu.setType(MenuItemType.MENU);
		MenuItem subMenu = new MenuItem();
		subMenu.setId(itemId);
		menu.setItems(Collections.singletonList(subMenu));
		assertEquals(new MenuItemToMolgenisUiMenuAdapter(menu, menu).getActiveItem().getId(), itemId);
	}

	@Test
	public void getBreadcrumb_menu()
	{
		String subSubMenuId = "subsubmenu";
		MenuItem subSubMenu = new MenuItem();
		subSubMenu.setId(subSubMenuId);
		subSubMenu.setType(MenuItemType.MENU);

		String subMenuId = "submenu";
		MenuItem subMenu = new MenuItem();
		subMenu.setId(subMenuId);
		subMenu.setType(MenuItemType.MENU);
		subMenu.setItems(Collections.singletonList(subSubMenu));

		String menuId = "menu";
		MenuItem menu = new MenuItem();
		menu.setId(menuId);
		menu.setType(MenuItemType.MENU);
		menu.setItems(Collections.singletonList(subMenu));

		List<UiMenu> breadcrumb = new MenuItemToMolgenisUiMenuAdapter(menu, menu).getBreadcrumb();
		assertEquals(breadcrumb.size(), 1);
		assertEquals(breadcrumb.get(0).getId(), menuId);
	}

	@Test
	public void getBreadcrumb_submenu()
	{
		String subMenuId = "submenu";
		MenuItem subMenu = new MenuItem();
		subMenu.setId(subMenuId);
		subMenu.setType(MenuItemType.MENU);

		String menuId = "menu";
		MenuItem menu = new MenuItem();
		menu.setId(menuId);
		menu.setType(MenuItemType.MENU);
		menu.setItems(Collections.singletonList(subMenu));

		List<UiMenu> breadcrumb = new MenuItemToMolgenisUiMenuAdapter(subMenu, menu).getBreadcrumb();
		assertEquals(breadcrumb.size(), 2);
		assertEquals(breadcrumb.get(0).getId(), menuId);
		assertEquals(breadcrumb.get(1).getId(), subMenuId);
	}

	@Test
	public void getBreadcrumb_subsubmenu()
	{
		String subSubMenuId = "subsubmenu";
		MenuItem subSubMenu = new MenuItem();
		subSubMenu.setId(subSubMenuId);
		subSubMenu.setType(MenuItemType.MENU);

		String subMenuId = "submenu";
		MenuItem subMenu = new MenuItem();
		subMenu.setId(subMenuId);
		subMenu.setType(MenuItemType.MENU);
		subMenu.setItems(Collections.singletonList(subSubMenu));

		String menuId = "menu";
		MenuItem menu = new MenuItem();
		menu.setId(menuId);
		menu.setType(MenuItemType.MENU);
		menu.setItems(Collections.singletonList(subMenu));

		List<UiMenu> breadcrumb = new MenuItemToMolgenisUiMenuAdapter(subSubMenu, menu).getBreadcrumb();
		assertEquals(breadcrumb.size(), 3);
		assertEquals(breadcrumb.get(0).getId(), menuId);
		assertEquals(breadcrumb.get(1).getId(), subMenuId);
		assertEquals(breadcrumb.get(2).getId(), subSubMenuId);
	}

	@Test
	public void getItems()
	{
		String subMenuId = "id0";
		MenuItem menu = new MenuItem();
		menu.setType(MenuItemType.MENU);
		MenuItem subMenu = new MenuItem();
		subMenu.setId(subMenuId);
		MenuItem menuItem = new MenuItem();
		String itemId = "id1";
		menuItem.setId(itemId);
		menu.setItems(Arrays.asList(subMenu, menuItem));
		assertEquals(new MenuItemToMolgenisUiMenuAdapter(menu, menu).getItems().size(), 2);
	}
}
