package org.molgenis.ui;

public interface MolgenisUi
{
	static final String DEFAULT_TITLE = "MOLGENIS";

	static final String KEY_HREF_LOGO = "app.href.logo";
	static final String KEY_HREF_CSS = "app.href.css";
	static final String KEY_TITLE = "app.name";

	/**
	 * Returns app title or 'MOLGENIS' if app title does not exist
	 * 
	 * @return
	 */
	String getTitle();

	String getHrefLogo();

	String getHrefCss();

	void setHrefLogo(String file);

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
