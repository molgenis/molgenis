package org.molgenis.omx.harmonization.mesh;

import javax.xml.bind.annotation.XmlElement;

public class Term
{
	String termUI;
	String name;

	public String getTermUI()
	{
		return termUI;
	}

	@XmlElement(name = "TermUI")
	public void setTermUI(String termUI)
	{
		this.termUI = termUI;
	}

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