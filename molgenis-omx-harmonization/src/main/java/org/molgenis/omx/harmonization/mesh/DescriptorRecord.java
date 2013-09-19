package org.molgenis.omx.harmonization.mesh;

import javax.xml.bind.annotation.XmlElement;

public class DescriptorRecord
{
	String descriptorUI;

	DescriptorName descriptorName;

	DateCreated dateCreated;

	ConceptList conceptList;

	public ConceptList getConceptList()
	{
		return conceptList;
	}

	@XmlElement(name = "ConceptList")
	public void setConceptList(ConceptList conceptList)
	{
		this.conceptList = conceptList;
	}

	public String getDescriptorUI()
	{
		return descriptorUI;
	}

	@XmlElement(name = "DescriptorUI")
	public void setDescriptorUI(String descriptorUI)
	{
		this.descriptorUI = descriptorUI;
	}

	public DescriptorName getDescriptorName()
	{
		return descriptorName;
	}

	@XmlElement(name = "DescriptorName")
	public void setDescriptorName(DescriptorName descriptorName)
	{
		this.descriptorName = descriptorName;
	}

	public DateCreated getDateCreated()
	{
		return dateCreated;
	}

	@XmlElement(name = "DateCreated")
	public void setDateCreated(DateCreated dateCreated)
	{
		this.dateCreated = dateCreated;
	}
}
