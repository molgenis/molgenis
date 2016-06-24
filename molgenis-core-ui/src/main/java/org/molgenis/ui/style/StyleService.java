package org.molgenis.ui.style;

import java.util.Set;

public interface StyleService
{
	/**
	 * Scans the file system for bootstrap theme css files and stores them in a list
	 * 
	 * @return A Set of available Styles found on the file system
	 */
	public Set<Style> getAvailableStyles();

	/**
	 * Set the runtime property that controls the css style with a selected style
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
	 * Returns a style that matches the name of the given input
	 * 
	 * @param styleName
	 * @return The Style that matches the name
	 */
	public Style getStyle(String styleName);
}
