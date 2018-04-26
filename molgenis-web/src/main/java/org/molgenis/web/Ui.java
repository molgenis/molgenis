package org.molgenis.web;

public interface Ui
{
	/**
	 * Returns the main menu
	 */
	UiMenu getMenu();

	/**
	 * Returns the menu with the given id
	 *
	 * @return menu or null if menu does not exist or is not accessible to user
	 */
	UiMenu getMenu(String menuId);

	String getMenuJson();
}
