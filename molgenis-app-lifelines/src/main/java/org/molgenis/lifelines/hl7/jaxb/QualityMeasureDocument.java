package org.molgenis.lifelines.hl7.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "QualityMeasureDocument")
public class QualityMeasureDocument
{
	private List<Component> components;

	@XmlElement(name = "component")
	public List<Component> getComponents()
	{
		return components;
	}

	public void setComponents(List<Component> components)
	{
		this.components = components;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("QualityMeasureDocument [components=").append(components).append("]");
		return builder.toString();
	}
}
