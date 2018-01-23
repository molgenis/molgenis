package org.molgenis.core.ui.menu;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class MenuMolgenisUiTest
{
	private MenuReaderService menuReaderService;
	private MenuMolgenisUi menuMolgenisUi;

	@BeforeMethod
	public void setUp()
	{
		menuReaderService = mock(MenuReaderService.class);
		menuMolgenisUi = new MenuMolgenisUi(menuReaderService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void MenuMolgenisUi()
	{
		new MenuMolgenisUi(null);
	}

	@Test
	public void getMenu()
	{
		Menu menu = new Menu();
		String id = "id";
		menu.setId(id);
		when(menuReaderService.getMenu()).thenReturn(menu);
		assertEquals(menuMolgenisUi.getMenu().getId(), id);
	}

	@Test
	public void getMenuString_topMenu()
	{
		Menu menu = new Menu();
		String id = "id";
		menu.setId(id);
		when(menuReaderService.getMenu()).thenReturn(menu);
		assertNotNull(menuMolgenisUi.getMenu(id));
	}

	@Test
	public void getMenuString_subMenu()
	{
		Menu menu = new Menu();
		menu.setId("id0");
		Menu subMenu = new Menu();
		String id = "id1";
		subMenu.setId(id);
		subMenu.setType(MenuItemType.MENU);
		menu.setItems(Collections.singletonList(subMenu));
		when(menuReaderService.getMenu()).thenReturn(menu);
		assertNotNull(menuMolgenisUi.getMenu(id));
	}
}
