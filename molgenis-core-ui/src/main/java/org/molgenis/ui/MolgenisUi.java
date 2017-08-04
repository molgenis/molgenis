package org.molgenis.ui;

import com.google.gson.Gson;
import org.molgenis.ui.menu.MenuUtils;
import org.molgenis.ui.menu.json.MenuItem;

public interface MolgenisUi
{
	/**
	 * Returns the main menu
	 */
	MolgenisUiMenu getMenu();

	/**
	 * Returns the menu with the given id
	 *
	 * @return menu or null if menu does not exist or is not accessible to user
	 */
	MolgenisUiMenu getMenu(String menuId);

	String getMenuJson();
}
