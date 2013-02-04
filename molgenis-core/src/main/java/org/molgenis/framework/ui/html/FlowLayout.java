package org.molgenis.framework.ui.html;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.util.tuple.Tuple;

/**
 * Flow layout layouts all elements in one flow, from left to right. If space
 * runs out the remaning elements are layouted on a second row.
 */
public class FlowLayout extends AbstractHtmlElement implements Layout
{
	/** List with layouted elements */
	private List<HtmlElement> elements = new ArrayList<HtmlElement>();

	/** Create a new flowlayout using an automatically assigned id */
	public FlowLayout()
	{
		super();
	}

	/** Create a new flowlayout using a user assigned id */
	public FlowLayout(String id)
	{
		super(id);
	}

	public List<HtmlElement> getElements()
	{
		return elements;
	}

	@Override
	public void setElements(List<HtmlElement> elements)
	{
		this.elements = elements;
	}

	@Override
	public void add(HtmlElement input)
	{
		if (input == null) throw new UnsupportedOperationException("intput cannot be null");
		this.elements.add(input);

	}

	@Override
	public String render()
	{
		StringBuilder strBuilder = new StringBuilder();
		for (HtmlElement i : this.getElements())
		{
			strBuilder.append(i.render());
		}
		return strBuilder.toString();
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return "";
	}

	@Override
	public String render(Tuple params) throws ParseException, HtmlInputException
	{
		throw new UnsupportedOperationException("not implemented");
	}
}
