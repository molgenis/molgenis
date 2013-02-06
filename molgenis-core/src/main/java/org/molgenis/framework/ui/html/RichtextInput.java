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
 * Input for rich html text editing (bold, italic, etc).
 * 
 * Thanks to http://www.tinymce.com/
 */
public class RichtextInput extends StringInput
{
	public RichtextInput(String name)
	{
		this(name, null);
	}

	public RichtextInput(String name, String value)
	{
		super(name, value);
		this.setMaxHeight(50);
		this.setMinHeight(3);
	}

	protected RichtextInput()
	{
	}

	@Override
	public String toHtml()
	{
		return String.format(
				"<textarea id=\"%s\" name=\"%s\" class=\"mceEditor %s\" cols=\"80\" rows=\"10\">%s</textarea>",
				getId(), getName(), (this.isNillable() ? "" : " required"), getValue());
	}

	/**
	 * Override because hyperlink must not be escaped
	 */
	@Override
	public String getHtmlValue()
	{
		return this.getValue();
	}
}
