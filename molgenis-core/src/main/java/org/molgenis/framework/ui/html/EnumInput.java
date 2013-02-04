/**
 * File: org.molgenis.framework.ui.html.EnumInput <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, GCC 2011 all rights reserved <br>
 * Changelog:
 * <ul>
 * <li> 2006-03-07, 1.0.0, DI Matthijssen
 * <li> 2006-05-14; 1.1.0; MA Swertz integration into Inveninge (and major rewrite)
 * <li> 2006-05-14; 1.2.0; RA Scheltema major rewrite + cleanup
 * </ul>
 */

package org.molgenis.framework.ui.html;

/**
 * Input for enumerated data.
 */
public class EnumInput extends SelectInput
{
	public EnumInput(String name, String value)
	{
		super(name, value);
	}

	public EnumInput(String name)
	{
		this(name, null);
	}

	protected EnumInput()
	{
		super();
	}
}
