package org.molgenis.omx.biobankconnect.mesh;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DescriptorName
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
