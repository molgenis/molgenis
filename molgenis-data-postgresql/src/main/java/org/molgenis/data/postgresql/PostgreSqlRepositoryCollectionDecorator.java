package org.molgenis.data.postgresql;

import com.google.common.collect.Lists;
import org.molgenis.data.AbstractRepositoryCollectionDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.molgenis.data.postgresql.identifier.Identifiable;
import org.molgenis.data.support.EntityTypeUtils;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Repository collection decorator that updates {@link EntityTypeRegistry} when entity types are create, updated or deleted.
 */
public class PostgreSqlRepositoryCollectionDecorator extends AbstractRepositoryCollectionDecorator
{
	private final EntityTypeRegistry entityTypeRegistry;

	PostgreSqlRepositoryCollectionDecorator(RepositoryCollection delegateRepositoryCollection,
			EntityTypeRegistry entityTypeRegistry)
	{
		super(delegateRepositoryCollection);
		this.entityTypeRegistry = requireNonNull(entityTypeRegistry);
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		Repository<Entity> repo = delegate().createRepository(entityType);
		entityTypeRegistry.registerEntityType(entityType.getId(), getReferenceTypeAttributes(entityType));
		return repo;
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		delegate().deleteRepository(entityType);
		entityTypeRegistry.unregisterEntityType(entityType.getId(), getReferenceTypeAttributes(entityType));
	}

	@Override
	public void updateRepository(EntityType entityType, EntityType updatedEntityType)
	{
		delegate().updateRepository(entityType, updatedEntityType);
		entityTypeRegistry.registerEntityType(updatedEntityType.getId(), getReferenceTypeAttributes(updatedEntityType));
	}

	@Override
	public void addAttribute(EntityType entityType, Attribute attribute)
	{
		if (EntityTypeUtils.isReferenceType(attribute))
		{
			List<Attribute> attributes = Lists.newArrayList(entityType.getAllAttributes());
			attributes.add(attribute);
			entityTypeRegistry.registerEntityType(entityType.getId(), getReferenceTypeAttributes(attributes.stream()));
		}
		delegate().addAttribute(entityType, attribute);
	}

	@Override
	public void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		List<Attribute> attributes = Lists.newArrayList(entityType.getAllAttributes());
		attributes.removeIf(candidate -> candidate.getName().equals(attr.getName()));
		attributes.add(updatedAttr);
		entityTypeRegistry.registerEntityType(entityType.getId(), getReferenceTypeAttributes(attributes.stream()));
		delegate().updateAttribute(entityType, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(EntityType entityType, Attribute attr)
	{
		List<Attribute> attributes = Lists.newArrayList(entityType.getAllAttributes());
		attributes.removeIf(candidate -> candidate.getName().equals(attr.getName()));
		entityTypeRegistry.registerEntityType(entityType.getId(), getReferenceTypeAttributes(attributes.stream()));
		delegate().deleteAttribute(entityType, attr);
	}

	private List<Identifiable> getReferenceTypeAttributes(EntityType entityType)
	{
		return getReferenceTypeAttributes(StreamSupport.stream(entityType.getAllAttributes().spliterator(), false));
	}

	private List<Identifiable> getReferenceTypeAttributes(Stream<Attribute> attributes)
	{
		return attributes.filter(EntityTypeUtils::isReferenceType)
						 .map(attr -> Identifiable.create(attr.getName(), attr.getIdentifier()))
						 .collect(toList());
	}
}
