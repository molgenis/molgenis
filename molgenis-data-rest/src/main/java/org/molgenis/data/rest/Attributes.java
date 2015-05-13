package org.molgenis.data.rest;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Attributes implements Iterable<Attribute>
{
	private final Map<String, Attribute> attributes;

	public Attributes(String attributesStr)
	{
		this.attributes = parse(attributesStr);
	}

	public Attribute getAttribute(String name)
	{
		return attributes.get(normalize(name));

	}

	public boolean contains(String name)
	{
		return attributes.containsKey(normalize(name));
	}

	@Override
	public Iterator<Attribute> iterator()
	{
		return attributes.values().iterator();
	}

	private Map<String, Attribute> parse(String attributesStr)
	{
		String[] tokens = attributesStr.split(",");
		Map<String, Attribute> attributeRequests = new LinkedHashMap<>();
		for (String token : tokens)
		{
			Attribute attribute = new Attribute(token);
			attributeRequests.put(normalize(attribute.getName()), attribute);
		}
		return attributeRequests;
	}

	private String normalize(String name)
	{
		return name.toLowerCase();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Attributes other = (Attributes) obj;
		if (attributes == null)
		{
			if (other.attributes != null) return false;
		}
		else if (!attributes.equals(other.attributes)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Attributes [attributes=" + attributes + "]";
	}
}