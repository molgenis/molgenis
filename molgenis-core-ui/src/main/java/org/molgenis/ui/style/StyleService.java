package org.molgenis.ui.style;

import java.util.List;

public interface StyleService
{
	/**
	 * Scans the bootswatch API and the file system for bootstrap theme css files stores them in a list
	 * 
	 * @return A list of available Styles found on the file system and via the bootswatch API
	 */
	public List<Style> getAvailableStyles();

	/**
	 * Set the runtime property that controls the css style via a selected style
	 * 
	 * @param styleName
	 */
	public void setSelectedStyle(String styleName);

	/**
	 * Get the style that is currently selected
	 * 
	 * @return Which style is currently selected
	 */
	public Style getSelectedStyle();
	
	/**
	 * Returns a style which matches the name of the given input
	 * 
	 * @param styleName
	 * @return The Style that matches the name
	 */
	public Style getStyle(String styleName);
}
