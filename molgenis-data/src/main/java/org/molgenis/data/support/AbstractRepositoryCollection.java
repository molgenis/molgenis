package org.molgenis.data.support;

import static autovalue.shaded.com.google.common.common.collect.Sets.immutableEnumSet;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollectionCapability;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;

/**
 * Base class for a {@link RepositoryCollection} that is not {@link org.molgenis.data.RepositoryCollectionCapability#WRITABLE}
 * and not {@link org.molgenis.data.RepositoryCollectionCapability#UPDATABLE}.
 */
public abstract class AbstractRepositoryCollection implements RepositoryCollection
{
	@Override
	public Set<RepositoryCollectionCapability> getCapabilities()
	{
		return immutableEnumSet(EnumSet.noneOf(RepositoryCollectionCapability.class));
	}

	@Override
	public Repository<Entity> createRepository(EntityMetaData entityMeta)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteRepository(EntityMetaData entityMeta)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAttribute(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Repository<Entity> getRepository(EntityMetaData entityMetaData)
	{
		return getRepository(entityMetaData.getName());
	}

	@Override
	public Stream<String> getLanguageCodes()
	{
		throw new UnsupportedOperationException();
	}
}
