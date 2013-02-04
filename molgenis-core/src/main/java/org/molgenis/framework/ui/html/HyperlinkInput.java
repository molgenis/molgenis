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

import java.text.ParseException;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for hyperlinks. This will automatically create a hyperlink to outside
 * information.
 */
public class HyperlinkInput extends HtmlInput<String>
{
	/**
	 * Construct HyperlinkInput with name
	 * 
	 * @param name
	 */
	public HyperlinkInput(String name)
	{
		this(name, null);
	}

	/**
	 * Construct HyperlinkInput with name and value
	 * 
	 * @param name
	 * @param value
	 */
	public HyperlinkInput(String name, String value)
	{
		super(name, value);
	}

	/**
	 * Construct HyperlinkInput with name, label, value, nillable, readonly.
	 * 
	 * @param name
	 * @param label
	 * @param value
	 * @param nillable
	 * @param readonly
	 */
	public HyperlinkInput(String name, String label, String value, boolean nillable, boolean readonly)
	{
		super(name, value);
		this.setLabel(label);
		this.setNillable(nillable);
		this.setReadonly(readonly);
	}

	/**
	 * Construct HyperlinkInput using a Tuple to set properties
	 * 
	 * @param properties
	 */
	public HyperlinkInput(Tuple properties)
	{
		this(properties.getString(NAME), properties.getString(LABEL), properties.getString(VALUE), properties
				.getBoolean(NILLABLE), properties.getBoolean(READONLY));
	}

	@Override
	public String getValue()
	{
		return "<a href=\"" + super.getValue() + "\">" + super.getValue() + "</a>";
	}

	/**
	 * Override because hyperlink must not be escaped
	 */
	@Override
	public String getHtmlValue()
	{
		return this.getValue();
	}

	@Override
	public String toHtml()
	{
		// String readonly = (isReadonly() ? "readonly class=\"readonly\" " :
		// "");

		// if (this.isHidden())
		// {
		StringInput input = new StringInput(this.getName(), super.getValue());
		input.setLabel(this.getLabel());
		input.setDescription(this.getDescription());
		input.setHidden(this.isHidden());
		input.setReadonly(this.isReadonly());
		return input.toHtml() + this.getValue();
		// }

		// return "<input id=\"" + getId() + "\" name=\"" + getName()
		// + "\" value=\"" + super.getValue() + "\" " + readonly + " />";
	}

	@Override
	public String toHtml(Tuple params) throws ParseException
	{
		return new HyperlinkInput(params).render();
	}

}
