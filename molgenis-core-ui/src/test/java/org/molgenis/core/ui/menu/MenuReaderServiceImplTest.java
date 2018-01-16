package org.molgenis.core.ui.menu;

import org.molgenis.settings.AppSettings;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class MenuReaderServiceImplTest
{
	@Test(expectedExceptions = NullPointerException.class)
	public void MenuReaderServiceImpl()
	{
		new MenuReaderServiceImpl(null);
	}

	@Test
	public void getMenu()
	{
		AppSettings appSettings = when(mock(AppSettings.class).getMenu()).thenReturn(
				"{\n" + "	\"type\": \"menu\",\n" + "	\"id\": \"menu\",\n" + "	\"label\": \"Menu\",\n"
						+ "	\"items\": [{\n" + "		\"type\": \"plugin\",\n" + "		\"id\": \"plugin0\",\n"
						+ "		\"label\": \"Plugin #0\",\n" + "		\"params\": \"a=0&b=1\"\n" + "	},\n"
						+ "	{\n" + "		\"type\": \"menu\",\n" + "		\"id\": \"submenu\",\n"
						+ "		\"label\": \"Submenu\",\n" + "		\"items\": [{\n" + "			\"type\": \"plugin\",\n"
						+ "			\"id\": \"plugin1\",\n" + "			\"label\": \"Plugin #1\"\n" + "		}]\n"
						+ "	}]\n" + "}").getMock();

		MenuItem item0 = new MenuItem();
		item0.setType(MenuItemType.PLUGIN);
		item0.setId("plugin0");
		item0.setLabel("Plugin #0");
		item0.setParams("a=0&b=1");

		MenuItem item1 = new MenuItem();
		item1.setType(MenuItemType.PLUGIN);
		item1.setId("plugin1");
		item1.setLabel("Plugin #1");

		MenuItem submenu = new MenuItem();
		submenu.setType(MenuItemType.MENU);
		submenu.setId("submenu");
		submenu.setLabel("Submenu");
		submenu.setItems(Collections.singletonList(item1));

		Menu menu = new Menu();
		menu.setId("menu");
		menu.setLabel("Menu");
		menu.setType(MenuItemType.MENU);
		menu.setItems(Arrays.asList(item0, submenu));
		assertEquals(new MenuReaderServiceImpl(appSettings).getMenu(), menu);
	}
}
