package org.molgenis.lifelines.hl7.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

public class Id
{
	private String extension;
	private String root;

	@XmlAttribute
	public String getExtension()
	{
		return extension;
	}

	public void setExtension(String extension)
	{
		this.extension = extension;
	}

	@XmlAttribute
	public String getRoot()
	{
		return root;
	}

	public void setRoot(String root)
	{
		this.root = root;
	}

	@Override
	public String toString()
	{
		return "Id [extension=" + extension + ", root=" + root + "]";
	}

}
