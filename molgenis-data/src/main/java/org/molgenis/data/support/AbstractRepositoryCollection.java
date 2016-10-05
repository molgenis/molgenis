package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollectionCapability;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.immutableEnumSet;

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
	public Repository<Entity> createRepository(EntityType entityType)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAttribute(EntityType entityType, AttributeMetaData attribute)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAttribute(EntityType entityType, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAttribute(EntityType entityType, AttributeMetaData attr)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Repository<Entity> getRepository(EntityType entityType)
	{
		return getRepository(entityType.getName());
	}

	@Override
	public Stream<String> getLanguageCodes()
	{
		throw new UnsupportedOperationException();
	}
}
