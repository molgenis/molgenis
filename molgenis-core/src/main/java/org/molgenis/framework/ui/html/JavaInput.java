/**
 * File: TextInput.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2006-03-08, 1.0.0, DI Matthijssen; Creation
 * <li>2006-05-14, 1.1.0, MA Swertz; Refectoring into Invengine.
 * </ul>
 * TODO look at the depreciated functions.
 */

package org.molgenis.framework.ui.html;

/**
 * Renders an editor for freemarker including code higlighting and line numbers.
 * 
 * Based on http://codemirror.net/ (thanks!)
 */
public class JavaInput extends CodeInput
{
	public JavaInput(String name)
	{
		this(name, null);
	}

	public JavaInput(String name, String value)
	{
		super(name, value, CodeInput.Parser.JAVA);
	}
}
