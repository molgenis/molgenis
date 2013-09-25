package org.molgenis.omx.harmonization.mesh;

import javax.xml.bind.annotation.XmlElement;

public class DescriptorRecord
{
	String descriptorUI;

	DescriptorName descriptorName;

	DateCreated dateCreated;

	ConceptList conceptList;

	TreeNumberList treeNumberList = new TreeNumberList();

	String annotation;

	public String getAnnotation()
	{
		return annotation;
	}

	@XmlElement(name = "Annotation")
	public void setAnnotation(String annotation)
	{
		this.annotation = annotation;
	}

	public TreeNumberList getTreeNumberList()
	{
		return treeNumberList;
	}

	@XmlElement(name = "TreeNumberList")
	public void setTreeNumberList(TreeNumberList treeNumberList)
	{
		this.treeNumberList = treeNumberList;
	}

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
