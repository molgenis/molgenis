package org.molgenis.ui;

public interface MolgenisUi
{
	String getTitle();

	String getHrefLogo();

	String getHrefCss();

	/**
	 * Returns the main menu
	 * 
	 * @return
	 */
	MolgenisUiMenu getMenu();

	/**
	 * Returns the menu with the given id
	 * 
	 * @return
	 */
	MolgenisUiMenu getMenu(String menuId);
}
