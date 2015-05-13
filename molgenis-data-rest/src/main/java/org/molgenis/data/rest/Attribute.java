package org.molgenis.data.rest;

public class Attribute
{
	private final String name;
	private final Attributes attributes;

	public Attribute(String attributeStr)
	{
		this.name = parseName(attributeStr);
		this.attributes = parseAttributes(attributeStr);
	}

	public String getName()
	{
		return name;
	}

	public Attributes getAttributes()
	{
		return attributes;
	}

	private String parseName(String attributeStr)
	{
		int idx = attributeStr.indexOf('[');
		return idx != -1 ? attributeStr.substring(0, idx) : attributeStr;
	}

	private Attributes parseAttributes(String attributeStr)
	{
		int idx = attributeStr.indexOf('[');
		return idx != -1 ? new Attributes(attributeStr.substring(idx + 1, attributeStr.length() - 1)) : null;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Attribute other = (Attribute) obj;
		if (attributes == null)
		{
			if (other.attributes != null) return false;
		}
		else if (!attributes.equals(other.attributes)) return false;
		if (name == null)
		{
			if (other.name != null) return false;
		}
		else if (!name.equals(other.name)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Attribute [name=" + name + ", attributes=" + attributes + "]";
	}
}
