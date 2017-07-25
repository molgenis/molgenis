package org.molgenis.data.rest.v2;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An AttributeFilter represents the value of the attrs parameter in a REST query.
 * <p>
 * The AttributeFilter allows you to specify which attributes should be fetched for each {@link Entity} that is
 * retrieved.
 * <p>
 * By default, the top level entity will always be fetched with all attributes, but you can also specify a list of
 * attribute names. Those will be added to the ID and label attributes, which will always be fetched.
 * <p>
 * Referenced entities will always be fetched using ID and label, but you can also specify a list of attribute names.
 * Those will be added to the ID and label attributes, which will always be fetched. You can also specify the special
 * selector `*` which will fetch all attributes for a referenced entity.
 */
class AttributeFilter implements Iterable<Entry<String, AttributeFilter>>
{
	private final Map<String, AttributeFilter> attributes;
	private boolean includeAllAttrs;
	private boolean includeIdAttr;
	private boolean includeLabelAttr;
	private AttributeFilter idAttrFilter;
	private AttributeFilter labelAttrFilter;

	public AttributeFilter()
	{
		this.attributes = new LinkedHashMap<>();
	}

	public boolean isIncludeAllAttrs()
	{
		return includeAllAttrs;
	}

	/**
	 * Indicates if this filter is {@link #includeAllAttrs}, and NO other attributes are selected.
	 */
	public boolean isStar()
	{
		return includeAllAttrs && (attributes == null || attributes.keySet().isEmpty());
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

	public AttributeFilter getAttributeFilter(EntityType entityType, Attribute attr)
	{
		if (idAttrFilter != null && attr.equals(entityType.getIdAttribute()))
		{
			return idAttrFilter;
		}
		else if (labelAttrFilter != null && attr.equals(entityType.getLabelAttribute()))
		{
			return labelAttrFilter;
		}
		else
		{
			return attributes.get(normalize(attr.getName()));
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
		return name;//.toLowerCase();
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