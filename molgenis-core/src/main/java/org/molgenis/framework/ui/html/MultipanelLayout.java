package org.molgenis.framework.ui.html;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.util.tuple.Tuple;

public abstract class MultipanelLayout extends AbstractHtmlElement implements Layout
{
	protected UiToolkit style = UiToolkit.JQUERY;
	protected Map<String, HtmlElement> elements = new LinkedHashMap<String, HtmlElement>();

	public MultipanelLayout(String id)
	{
		super(id);
	}

	@Override
	public void add(HtmlElement element)
	{
		this.elements.put("Panel" + this.elements.size(), element);
	}

	public void add(String name, HtmlElement element)
	{
		this.elements.put(name, element);
	}

	public Map<String, HtmlElement> getElements()
	{
		return elements;
	}

	public void setElements(Map<String, HtmlElement> elements)
	{
		this.elements = elements;
	}

	@Override
	public void set(Tuple properties) throws HtmlInputException
	{
		// todo: need to think on how to implement
		throw new UnsupportedOperationException();

	}

	@Override
	public void setElements(List<HtmlElement> elements)
	{
		for (HtmlElement e : elements)
			this.add(e.getId(), e);
	}

	@Override
	public String render(Tuple params) throws ParseException, HtmlInputException
	{
		throw new UnsupportedOperationException("not implemented");
	}
}
