package org.molgenis.data.support;

import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;

import java.util.Iterator;

import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.Package;

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

	/**
	 * Builds and returns an entity full name based on a package and a simpleName
	 * 
	 * @param package_
	 * @param simpleName
	 * @return String entity full name
	 */
	public static String buildFullName(Package package_, String simpleName)
	{
		if (package_ != null && !PACKAGE_DEFAULT.equals(package_.getName()))
		{
			StringBuilder sb = new StringBuilder();
			sb.append(package_.getName());
			sb.append(PACKAGE_SEPARATOR);
			sb.append(simpleName);
			return sb.toString();
		}
		else
		{
			return simpleName;
		}
	}

	public static Fetch createFetchForReindexing(EntityMetaData refEntityMetaData)
	{
		Fetch fetch = new Fetch();
		for (AttributeMetaData attr : refEntityMetaData.getAtomicAttributes())
		{
			if (attr.getRefEntity() != null)
			{
				Fetch attributeFetch = new Fetch();
				for (AttributeMetaData refAttr : attr.getRefEntity().getAtomicAttributes())
				{
					attributeFetch.field(refAttr.getName());
				}
				fetch.field(attr.getName(), attributeFetch);
			}
			else
			{
				fetch.field(attr.getName());
			}

		}
		return fetch;
	}
}
