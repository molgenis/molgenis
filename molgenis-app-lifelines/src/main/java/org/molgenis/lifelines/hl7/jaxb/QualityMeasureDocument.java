package org.molgenis.lifelines.hl7.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "QualityMeasureDocument")
public class QualityMeasureDocument
{
	private List<Component> components;
	private Id id;
	private String name;

	@XmlElement(name = "component")
	public List<Component> getComponents()
	{
		return components;
	}

	public void setComponents(List<Component> components)
	{
		this.components = components;
	}

	@XmlElement(name = "id")
	public Id getId()
	{
		return id;
	}

	public void setId(Id id)
	{
		this.id = id;
	}

	@XmlElement(name = "name")
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return "QualityMeasureDocument [components=" + components + ", id=" + id + ", name=" + name + "]";
	}

}
