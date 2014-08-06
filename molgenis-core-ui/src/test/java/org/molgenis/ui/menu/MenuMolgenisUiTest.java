package org.molgenis.ui.menu;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Collections;

import org.molgenis.framework.server.MolgenisSettings;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MenuMolgenisUiTest
{
	private MolgenisSettings molgenisSettings;
	private MenuReaderService menuReaderService;
	private MenuMolgenisUi menuMolgenisUi;

	@BeforeMethod
	public void setUp()
	{
		molgenisSettings = mock(MolgenisSettings.class);
		menuReaderService = mock(MenuReaderService.class);
		menuMolgenisUi = new MenuMolgenisUi(molgenisSettings, menuReaderService);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MenuMolgenisUi()
	{
		new MenuMolgenisUi(null, null);
	}

	@Test
	public void getHrefCss()
	{
		String href = "href";
		when(molgenisSettings.getProperty(MenuMolgenisUi.KEY_HREF_CSS)).thenReturn(href);
		assertEquals(menuMolgenisUi.getHrefCss(), href);
	}

	@Test
	public void getHrefLogo()
	{
		String href = "href";
		when(molgenisSettings.getProperty(MenuMolgenisUi.KEY_HREF_LOGO)).thenReturn(href);
		assertEquals(menuMolgenisUi.getHrefLogo(), href);
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
		menu.setItems(Collections.<MenuItem> singletonList(subMenu));
		when(menuReaderService.getMenu()).thenReturn(menu);
		assertNotNull(menuMolgenisUi.getMenu(id));
	}

	@Test
	public void getTitle()
	{
		String title = "title";
		when(molgenisSettings.getProperty(MenuMolgenisUi.KEY_TITLE)).thenReturn(title);
		assertEquals(menuMolgenisUi.getTitle(), title);
	}
}
