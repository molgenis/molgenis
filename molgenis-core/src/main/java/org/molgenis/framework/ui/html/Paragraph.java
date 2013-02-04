package org.molgenis.framework.ui.html;

import java.text.ParseException;
import java.util.UUID;

import org.molgenis.util.tuple.Tuple;

/**
 * Show text within a paragraph (html
 * <p>
 * tag).
 */
public class Paragraph extends HtmlInput<String>
{
	public Paragraph(String value)
	{
		this(UUID.randomUUID().toString(), value);
	}

	public Paragraph(String name, String value)
	{
		super(name, value);
		this.setLabel("");
	}

	@Override
	public String toHtml()
	{
		// Don't escape special characters, so user can insert html into the
		// paragraph
		return "<p id=\"" + getId() + "\" name=\"" + getName() + "\"" + tabIndex + " >" + getValue(false) + "</p>";
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		// pretty useless as you better use <p> inside freemarker
		throw new UnsupportedOperationException();
	}
}
