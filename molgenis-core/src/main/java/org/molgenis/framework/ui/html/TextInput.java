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
 * Input for strings that renders as textarea. (Undeprecated because of
 * practical use differs)
 */
public class TextInput extends StringInput
{
	public TextInput(String name)
	{
		this(name, null);
	}

	public TextInput(String name, String value)
	{
		super(name, value);
		this.setMaxHeight(50);
		this.setMinHeight(3);
	}

	protected TextInput()
	{
	}
}
