package org.molgenis.omx.harmonization.mesh;

import javax.xml.bind.annotation.XmlElement;

public class ConceptName
{
	String name;

	public String getName()
	{
		return name;
	}

	@XmlElement(name = "String")
	public void setName(String name)
	{
		this.name = name;
	}
}
