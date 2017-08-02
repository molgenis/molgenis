package org.molgenis.data.security.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Attribute repository decorator that marks attributes as read-only for the current user based on permissions.
 */
public class AttributeRepositorySecurityDecorator extends AbstractRepositoryDecorator<Attribute>
{
	private final PermissionService permissionService;

	public AttributeRepositorySecurityDecorator(Repository<Attribute> delegateRepository,
			PermissionService permissionService)
	{
		super(delegateRepository);
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	public Iterator<Attribute> iterator()
	{
		Iterable<Attribute> attributeIterable = delegate()::iterator;
		return StreamSupport.stream(attributeIterable.spliterator(), false).map(this::toPermittedAttribute).iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Attribute>> consumer, int batchSize)
	{
		MappedConsumer mappedConsumer = new MappedConsumer(consumer, this);
		delegate().forEachBatched(fetch, mappedConsumer::map, batchSize);
		super.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public Stream<Attribute> findAll(Query<Attribute> q)
	{
		return delegate().findAll(q).map(this::toPermittedAttribute);
	}

	@Override
	public Attribute findOne(Query<Attribute> q)
	{
		return toPermittedAttribute(delegate().findOne(q));
	}

	@Override
	public Attribute findOneById(Object id)
	{
		return toPermittedAttribute(delegate().findOneById(id));
	}

	@Override
	public Attribute findOneById(Object id, Fetch fetch)
	{
		return toPermittedAttribute(delegate().findOneById(id, fetch));
	}

	@Override
	public Stream<Attribute> findAll(Stream<Object> ids)
	{
		return delegate().findAll(ids).map(this::toPermittedAttribute);
	}

	@Override
	public Stream<Attribute> findAll(Stream<Object> ids, Fetch fetch)
	{
		return delegate().findAll(ids, fetch).map(this::toPermittedAttribute);
	}

	private Attribute toPermittedAttribute(Attribute attribute)
	{
		if (attribute != null)
		{
			String entityTypeId = attribute.getEntityType().getId();
			Object entityId = attribute.getIdValue();
			if (!permissionService.hasPermissionOnEntity(entityTypeId, entityId, Permission.WRITE))
			{
				attribute.setReadOnly(true);
			}
		}
		return attribute;
	}

	private static class MappedConsumer
	{
		private final Consumer<List<Attribute>> consumer;
		private final AttributeRepositorySecurityDecorator attributeRepositorySecurityDecorator;

		MappedConsumer(Consumer<List<Attribute>> consumer,
				AttributeRepositorySecurityDecorator attributeRepositorySecurityDecorator)
		{
			this.consumer = requireNonNull(consumer);
			this.attributeRepositorySecurityDecorator = requireNonNull(attributeRepositorySecurityDecorator);
		}

		public void map(List<Attribute> attributes)
		{
			Stream<Attribute> filteredEntities = attributes.stream()
														   .map(attributeRepositorySecurityDecorator::toPermittedAttribute);
			consumer.accept(filteredEntities.collect(toList()));
		}
	}
}
