package org.molgenis.framework.ui.html;

import java.text.ParseException;

import org.molgenis.util.tuple.Tuple;

/**
 * Show text within a div (html <code>div</code> tag).
 */
public class Division extends HtmlInput<String>
{
	public Division(String name)
	{
		this(name, null);
	}

	public Division(String name, String value)
	{
		super(name, value);
		this.setLabel("");
	}

	@Override
	public String toHtml()
	{
		// Don't escape special characters, so user can insert html into the div
		return "<div id=\"" + getId() + "\" name=\"" + getName() + "\"" + tabIndex + " >" + getValue(false) + "</p>";
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		// pretty useless as you better use <div> inside freemarker
		throw new UnsupportedOperationException();
	}
}
