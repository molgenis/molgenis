package org.molgenis.lifelines.hl7.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class Text
{
	private List<String> items;

	@XmlElementWrapper(name = "list")
	@XmlElement(name = "item")
	public List<String> getItems()
	{
		return items;
	}

	public void setItems(List<String> items)
	{
		this.items = items;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Text [items=").append(items).append("]");
		return builder.toString();
	}
}
