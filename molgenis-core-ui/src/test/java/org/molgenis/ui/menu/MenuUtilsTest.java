package org.molgenis.ui.menu;

import static org.molgenis.ui.menu.MenuItemType.MENU;
import static org.molgenis.ui.menu.MenuItemType.PLUGIN;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
}
