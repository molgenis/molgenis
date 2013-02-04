/**
 * File: invengine.screen.View <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-05-07; 1.0.0; MA Swertz; Creation.
 * <li>2005-12-02; 1.0.0; RA Scheltema; Moved to the new structure, made the
 * method reset abstract and added documentation.
 * <li>2006-5-14; 1.1.0; MA Swertz; refactored to separate controller and view
 * </ul>
 */

package org.molgenis.framework.ui;

// jdk

/**
 * Base-class for a screen displaying information from the invengine system to
 * the user.
 */
public abstract class SimpleScreenView<M extends ScreenModel> implements ScreenView
{
	String customHtmlHeaders = "";
	M model = null;

	public SimpleScreenView(M model)
	{
		this.model = model;
	}

	public void setCustomHtmlHeaders(String customHtmlHeaders)
	{
		this.customHtmlHeaders = customHtmlHeaders;
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return this.customHtmlHeaders;
	}

	@Override
	public abstract String render();

	public M getModel()
	{
		return model;
	}

	public void setModel(M model)
	{
		this.model = model;
	}
}
