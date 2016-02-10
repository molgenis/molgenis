package org.molgenis.data.support;

import static java.util.stream.StreamSupport.stream;

import java.util.Iterator;

import org.molgenis.data.AttributeMetaData;

public class EntityMetaDataUtils
{
	private EntityMetaDataUtils()
	{
	}

	/**
	 * Returns attribute names for the given attributes
	 * 
	 * @return attribute names
	 */
	public static Iterable<String> getAttributeNames(Iterable<AttributeMetaData> attrs)
	{
		return new Iterable<String>()
		{
			@Override
			public Iterator<String> iterator()
			{
				return stream(attrs.spliterator(), false).map(AttributeMetaData::getName).iterator();
			}
		};
	}
}
