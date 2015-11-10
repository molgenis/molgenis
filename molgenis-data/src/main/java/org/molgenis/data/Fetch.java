package org.molgenis.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link Fetch} that defines which entity attributes to retrieve. For attributes referring to entities a Fetch can be
 * supplied that defines which entity attributes to retrieve for the referred entity.
 */
public class Fetch implements Iterable<Entry<String, Fetch>>
{
	private final Map<String, Fetch> attrFetchMap;

	public Fetch()
	{
		this.attrFetchMap = new LinkedHashMap<>();
	}

	public Fetch field(String field)
	{
		return field(field, null);
	}

	public Fetch field(String field, Fetch fetch)
	{
		attrFetchMap.put(field, fetch);
		return this;
	}

	public Fetch getFetch(String field)
	{
		return attrFetchMap.get(field);
	}

	public Fetch getFetch(AttributeMetaData attr)
	{
		return getFetch(attr.getName());
	}

	public boolean hasField(String field)
	{
		return attrFetchMap.containsKey(field);
	}

	public boolean hasField(AttributeMetaData attr)
	{
		return hasField(attr.getName());
	}

	public Set<String> getFields()
	{
		return Collections.unmodifiableMap(attrFetchMap).keySet();
	}

	@Override
	public Iterator<Entry<String, Fetch>> iterator()
	{
		return Collections.unmodifiableMap(attrFetchMap).entrySet().iterator();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attrFetchMap == null) ? 0 : attrFetchMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Fetch other = (Fetch) obj;
		if (attrFetchMap == null)
		{
			if (other.attrFetchMap != null) return false;
		}
		else if (!attrFetchMap.equals(other.attrFetchMap)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		toStringRec(builder, this);
		return builder.toString();
	}

	private void toStringRec(StringBuilder builder, Fetch fetch)
	{
		builder.append('(');
		for (Iterator<Map.Entry<String, Fetch>> it = fetch.iterator(); it.hasNext();)
		{
			Entry<String, Fetch> entry = it.next();
			builder.append(entry.getKey());

			Fetch subFetch = entry.getValue();
			if (subFetch != null)
			{
				toStringRec(builder, subFetch);
			}

			if (it.hasNext())
			{
				builder.append(',');
			}
		}
		builder.append(')');
	}
}
