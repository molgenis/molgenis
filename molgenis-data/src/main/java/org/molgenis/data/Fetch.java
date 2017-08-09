package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;

import java.util.*;
import java.util.Map.Entry;

/**
 * {@link Fetch} that defines which entity attributes to retrieve. For attributes referring to entities a Fetch can be
 * supplied that defines which entity attributes to retrieve for the referred entity.
 * <p>
 * A null Fetch means that all attributes should be retrieved.
 */
public class Fetch implements Iterable<Entry<String, Fetch>>
{
	private final Map<String, Fetch> attrFetchMap;

	/**
	 * Creates an empty Fetch.
	 */
	public Fetch()
	{
		this.attrFetchMap = new LinkedHashMap<>();
	}

	/**
	 * Updates this Fetch, adding a single field. If the field is a reference, the reference will be fetched with a null
	 * Fetch, which means that all attributes will be fetched.
	 *
	 * @param field the name of the field to fetch
	 * @return this Fetch, updated
	 */
	public Fetch field(String field)
	{
		return field(field, null);
	}

	/**
	 * Updates this fetch, adding a single field. If the field is a reference, the reference will be fetched with the
	 * Fetch that is provided.
	 *
	 * @param field the name of the field to fetch
	 * @param fetch the fetch to use for this field, if the field is a reference
	 * @return this Fetch, updated
	 */
	public Fetch field(String field, Fetch fetch)
	{
		attrFetchMap.put(field, fetch);
		return this;
	}

	/**
	 * Retrieves the Fetch for a particular field
	 *
	 * @param field the field for which the Fetch is retrieved
	 * @return the Fetch for this field, or null if none was provided
	 */
	public Fetch getFetch(String field)
	{
		return attrFetchMap.get(field);
	}

	/**
	 * Retrieves the Fetch for an attribute
	 *
	 * @param attr
	 */
	public Fetch getFetch(Attribute attr)
	{
		return getFetch(attr.getName());
	}

	/**
	 * Indicates if a field is included in this Fetch.
	 *
	 * @param field the field that is queried
	 * @return true if the field is included in this Fetch, otherwise false
	 */
	public boolean hasField(String field)
	{
		return attrFetchMap.containsKey(field);
	}

	/**
	 * Indicates if a field is included in this Fetch
	 *
	 * @param attr {@link Attribute} for the field
	 * @return true if the field is included in this Fetch, otherwise false
	 */
	public boolean hasField(Attribute attr)
	{
		return hasField(attr.getName());
	}

	/**
	 * Retrieves the fields included in this Fetch
	 *
	 * @return {@link Set} containing the names of all fields included in this Fetch
	 */
	public Set<String> getFields()
	{
		return Collections.unmodifiableMap(attrFetchMap).keySet();
	}

	/**
	 * Iterates over all fields in this fetch. The key is the field name, the value is the {@link Fetch} for that field,
	 * or null if no Fetch is provided for that field.
	 *
	 * @return {@link Iterator} over all {@link Entry}s in this fetch.
	 */
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
		for (Iterator<Map.Entry<String, Fetch>> it = fetch.iterator(); it.hasNext(); )
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
