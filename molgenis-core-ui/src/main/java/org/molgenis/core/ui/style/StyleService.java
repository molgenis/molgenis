package org.molgenis.core.ui.style;

import org.springframework.core.io.FileSystemResource;

import java.io.InputStream;
import java.util.Set;

public interface StyleService
{
	/**
	 * Fetches the available styleSheet from the data store and stores them in a list
	 *
	 * @return A Set of available Styles found on the file system
	 */
	Set<Style> getAvailableStyles();

	/**
	 * Add a bootstrap theme
	 *
	 * @param styleId             the them identifier used in the request
	 * @param bootstrap3FileName  the name to use as fileName for the bootstrap 3 file
	 * @param bootstrap3StyleData the bootstrap 3 style data
	 * @param bootstrap4FileName  the name to use as fileName for the bootstrap 4 file, this is optional
	 * @param bootstrap4StyleData the bootstrap 4 style data, this is optional
	 */
	Style addStyle(String styleId, String bootstrap3FileName, InputStream bootstrap3StyleData,
			String bootstrap4FileName, InputStream bootstrap4StyleData) throws MolgenisStyleException;

	/**
	 * Set the runtime property that controls the css style with a selected style
	 */
	void setSelectedStyle(String styleName);

	/**
	 * Get the style that is currently selected
	 *
	 * @return Which style is currently selected
	 */
	Style getSelectedStyle();

	/**
	 * Returns a style that matches the name of the given input
	 *
	 * @return The Style that matches the name
	 */
	Style getStyle(String styleName);

	/**
	 * Get the styleSheet data for a given theme and bootstrap version ( 3 or 4 )
	 *
	 * @param styleName        the theme identifier
	 * @param bootstrapVersion the bootstrap version ( 3 or 4)
	 * @return The theme data, as setting the bootstrap 4 theme is optional the fallback bootstrap 4 theme data is
	 * returned in case there is not theme data for the bootstrap 4 version.
	 */
	FileSystemResource getThemeData(String styleName, BootstrapVersion bootstrapVersion) throws MolgenisStyleException;

	/**
	 * Find the styleSheet for a given theme name
	 *
	 * @param themeName the name of the theme
	 * @return The styleSheet entity or null in case no theme matching the given name is found
	 */
	StyleSheet findThemeByName(String themeName);
}
