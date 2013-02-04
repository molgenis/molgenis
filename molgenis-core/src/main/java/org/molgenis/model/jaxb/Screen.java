package org.molgenis.model.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
// so use fields bypassing get/set
public class Screen
{
	String name;

	public synchronized String getName()
	{
		return name;
	}

	public synchronized void setName(String name)
	{
		this.name = name;
	}
}
