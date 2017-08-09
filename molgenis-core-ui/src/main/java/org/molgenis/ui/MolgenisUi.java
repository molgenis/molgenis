package org.molgenis.ui;

public interface MolgenisUi
{
	/**
	 * Returns the main menu
	 *
	 * @return
	 */
	MolgenisUiMenu getMenu();

	/**
	 * Returns the menu with the given id
	 *
	 * @return menu or null if menu does not exist or is not accessible to user
	 */
	MolgenisUiMenu getMenu(String menuId);
}
