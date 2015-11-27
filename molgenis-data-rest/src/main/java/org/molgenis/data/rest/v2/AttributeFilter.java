package org.molgenis.data.rest.v2;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.data.AttributeMetaData;

class AttributeFilter implements Iterable<Entry<String, AttributeFilter>>
{
	public static final AttributeFilter ALL_ATTRS_FILTER = new AttributeFilter().setIncludeAllAttrs(true);

	private final Map<String, AttributeFilter> attributes;
	private boolean includeAllAttrs;
	private boolean includeIdAttr;
	private boolean includeLabelAttr;
	private AttributeFilter idAttrFilter;
	private AttributeFilter labelAttrFilter;

	public AttributeFilter()
	{
		this.attributes = new LinkedHashMap<String, AttributeFilter>();
	}

	public boolean isIncludeAllAttrs()
	{
		return includeAllAttrs;
	}

	AttributeFilter setIncludeAllAttrs(boolean includeAllAttrs)
	{
		this.includeAllAttrs = includeAllAttrs;
		return this;
	}

	public boolean isIncludeIdAttr()
	{
		return includeIdAttr;
	}

	public AttributeFilter setIncludeIdAttr(boolean includeIdAttr)
	{
		return setIncludeIdAttr(includeIdAttr, null);
	}

	public AttributeFilter setIncludeIdAttr(boolean includeIdAttr, AttributeFilter idAttrFilter)
	{
		this.includeIdAttr = includeIdAttr;
		this.idAttrFilter = idAttrFilter;
		return this;
	}

	public boolean isIncludeLabelAttr()
	{
		return includeLabelAttr;
	}

	public AttributeFilter setIncludeLabelAttr(boolean includeLabelAttr)
	{
		return setIncludeLabelAttr(includeLabelAttr, null);
	}

	public AttributeFilter setIncludeLabelAttr(boolean includeLabelAttr, AttributeFilter labelAttrFilter)
	{
		this.includeLabelAttr = includeLabelAttr;
		this.labelAttrFilter = labelAttrFilter;
		return this;
	}

	public AttributeFilter getAttributeFilter(AttributeMetaData attr)
	{
		if (idAttrFilter != null && attr.isIdAtrribute())
		{
			return idAttrFilter;
		}
		else if (labelAttrFilter != null && attr.isLabelAttribute())
		{
			return labelAttrFilter;
		}
		else
		{
			return attributes.get(normalize(attr.getName()));
		}
	}

	public boolean includeAttribute(AttributeMetaData attr)
	{
		if (this.includeAllAttrs)
		{
			return true;
		}
		else if (this.includeIdAttr && attr.isIdAtrribute())
		{
			return true;
		}
		else if (this.includeLabelAttr && attr.isLabelAttribute())
		{
			return true;
		}
		else
		{
			return attributes.containsKey(normalize(attr.getName()));
		}
	}

	@Override
	public Iterator<Entry<String, AttributeFilter>> iterator()
	{
		return Collections.unmodifiableMap(attributes).entrySet().iterator();
	}

	public AttributeFilter add(String name)
	{
		return add(name, null);
	}

	public AttributeFilter add(String name, AttributeFilter attributeSelection)
	{
		attributes.put(normalize(name), attributeSelection);
		return this;
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
		result = prime * result + (includeAllAttrs ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AttributeFilter other = (AttributeFilter) obj;
		if (attributes == null)
		{
			if (other.attributes != null) return false;
		}
		else if (!attributes.equals(other.attributes)) return false;
		if (includeAllAttrs != other.includeAllAttrs) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "AttributeFilter [attributes=" + attributes + ", includeAllAttrs=" + includeAllAttrs + "]";
	}
}