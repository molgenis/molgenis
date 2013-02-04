package org.molgenis.lifelines.hl7.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class Component
{
	private List<Section> sections;

	@XmlElement(name = "section")
	public List<Section> getSections()
	{
		return sections;
	}

	public void setSections(List<Section> sections)
	{
		this.sections = sections;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Component [sections=").append(sections).append("]");
		return builder.toString();
	}
}
