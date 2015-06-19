package org.molgenis.catalogue.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ShoppingCart
{
	private String entityName;
	private final SortedSet<String> attributes = new TreeSet<String>();

	public String getEntityName()
	{
		return entityName;
	}

	public Set<String> getAttributes()
	{
		return Collections.unmodifiableSet(attributes);
	}

	public ShoppingCart addAttributes(String entityName, List<String> names)
	{
		setEntityName(entityName);
		attributes.addAll(names);
		return this;
	}

	public ShoppingCart addAttribute(String entityName, String name)
	{
		setEntityName(entityName);
		attributes.add(name);
		return this;
	}

	public ShoppingCart setEntityName(String entityName)
	{
		if (this.entityName == null)
		{
			this.entityName = entityName;
		}
		if (!this.entityName.equals(entityName))
		{
			this.entityName = entityName;
			attributes.clear();
		}
		return this;
	}

	public ShoppingCart removeAttribute(String name)
	{
		attributes.remove(name);
		return this;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((entityName == null) ? 0 : entityName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ShoppingCart other = (ShoppingCart) obj;
		if (attributes == null)
		{
			if (other.attributes != null) return false;
		}
		else if (!attributes.equals(other.attributes)) return false;
		if (entityName == null)
		{
			if (other.entityName != null) return false;
		}
		else if (!entityName.equals(other.entityName)) return false;
		return true;
	}

	public boolean isEmpty()
	{
		return entityName == null || attributes.isEmpty();
	}

	public ShoppingCart clear()
	{
		entityName = null;
		attributes.clear();
		return this;
	}

}
