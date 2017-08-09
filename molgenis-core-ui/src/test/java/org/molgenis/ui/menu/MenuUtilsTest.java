package org.molgenis.ui.menu;

import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.ui.MolgenisUiMenuItem;
import org.molgenis.ui.MolgenisUiMenuItemType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.ui.menu.MenuItemType.MENU;
import static org.molgenis.ui.menu.MenuItemType.PLUGIN;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class MenuUtilsTest
{
	private Menu menu;

	@BeforeMethod
	public void setUp()
	{
		menu = new Menu();

		MenuItem p30 = new MenuItem(PLUGIN, "p3_0", "lbl");
		MenuItem p31 = new MenuItem(PLUGIN, "p3_1", "lbl");

		MenuItem p20 = new MenuItem(MENU, "p2_0", "lbl");
		p20.setItems(Arrays.asList(p30, p31));
		MenuItem p21 = new MenuItem(PLUGIN, "p2_1", "lbl");

		MenuItem p10 = new MenuItem(PLUGIN, "p1_0", "lbl");
		MenuItem p11 = new MenuItem(MENU, "p1_1", "lbl");
		p11.setItems(Arrays.asList(p20, p21));

		menu.setId("root");
		menu.setType(MENU);
		menu.setItems(Arrays.asList(p10, p11));
	}

	@Test
	public void findMenuItemPath()
	{
		assertEquals(MenuUtils.findMenuItemPath("p1_0", menu), "/menu/root/p1_0");
		assertEquals(MenuUtils.findMenuItemPath("p1_1", menu), "/menu/root/p1_1");
		assertEquals(MenuUtils.findMenuItemPath("p2_0", menu), "/menu/p1_1/p2_0");
		assertEquals(MenuUtils.findMenuItemPath("p2_1", menu), "/menu/p1_1/p2_1");
		assertEquals(MenuUtils.findMenuItemPath("p3_0", menu), "/menu/p2_0/p3_0");
		assertEquals(MenuUtils.findMenuItemPath("p3_1", menu), "/menu/p2_0/p3_1");
		assertNull(MenuUtils.findMenuItemPath("non_existing", menu));
	}

	@Test
	public void getMenuJson()
	{
		MolgenisUiMenu menu = mock(MolgenisUiMenu.class);
		when(menu.getType()).thenReturn(MolgenisUiMenuItemType.MENU);
		when(menu.getId()).thenReturn("main");
		when(menu.getName()).thenReturn("mainmenu");
		when(menu.getUrl()).thenReturn("/main");
		MolgenisUiMenu submenu = mock(MolgenisUiMenu.class);
		when(submenu.getType()).thenReturn(MolgenisUiMenuItemType.MENU);
		when(submenu.getId()).thenReturn("sub");
		when(submenu.getName()).thenReturn("submenu");
		when(submenu.getUrl()).thenReturn("/sub");
		MolgenisUiMenuItem menuItem1 = mock(MolgenisUiMenuItem.class);
		when(menuItem1.getType()).thenReturn(MolgenisUiMenuItemType.PLUGIN);
		when(menuItem1.getId()).thenReturn("item1");
		when(menuItem1.getName()).thenReturn("label1");
		when(menuItem1.getUrl()).thenReturn("/item1?test=test");
		MolgenisUiMenuItem menuItem2 = mock(MolgenisUiMenuItem.class);
		when(menuItem2.getType()).thenReturn(MolgenisUiMenuItemType.PLUGIN);
		when(menuItem2.getId()).thenReturn("item2");
		when(menuItem2.getName()).thenReturn("label2");
		when(menuItem2.getUrl()).thenReturn("/item2");
		MolgenisUiMenuItem submenuItem1 = mock(MolgenisUiMenuItem.class);
		when(submenuItem1.getType()).thenReturn(MolgenisUiMenuItemType.PLUGIN);
		when(submenuItem1.getId()).thenReturn("subitem1");
		when(submenuItem1.getName()).thenReturn("sub2");
		when(submenuItem1.getUrl()).thenReturn("/sub2");
		MolgenisUiMenuItem menuItem3 = mock(MolgenisUiMenuItem.class);
		when(menuItem3.getType()).thenReturn(MolgenisUiMenuItemType.PLUGIN);
		when(menuItem3.getId()).thenReturn("item3");
		when(menuItem3.getName()).thenReturn("label3");
		when(menuItem3.getUrl()).thenReturn("/item3");

		when(menu.getItems()).thenReturn(Arrays.asList(menuItem1, menuItem2, submenu, menuItem3));
		when(submenu.getItems()).thenReturn(Arrays.asList(submenuItem1));
		String expected = "{\"id\":\"main\",\"label\":\"mainmenu\",\"href\":\"/main\",\"type\":\"MENU\",\"items\":[{\"id\":\"item1\",\"label\":\"label1\",\"href\":\"/item1?test\\u003dtest\",\"type\":\"PLUGIN\"},{\"id\":\"item2\",\"label\":\"label2\",\"href\":\"/item2\",\"type\":\"PLUGIN\"},{\"id\":\"sub\",\"label\":\"submenu\",\"href\":\"/sub\",\"type\":\"MENU\",\"items\":[{\"id\":\"subitem1\",\"label\":\"sub2\",\"href\":\"/sub2\",\"type\":\"PLUGIN\"}]},{\"id\":\"item3\",\"label\":\"label3\",\"href\":\"/item3\",\"type\":\"PLUGIN\"}]}";
		assertEquals(MenuUtils.getMenuJson(menu), expected);
	}
}
