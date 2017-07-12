package org.molgenis.data.security.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.security.acl.EntityAclService;
import org.molgenis.data.security.acl.SecurityId;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class AttributeRepositorySecurityDecorator extends AbstractRepositoryDecorator<Attribute>
{
	private final Repository<Attribute> decoratedRepo;
	private final EntityAclService entityAclService;

	public AttributeRepositorySecurityDecorator(Repository<Attribute> decoratedRepo, EntityAclService entityAclService)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.entityAclService = requireNonNull(entityAclService);
	}

	@Override
	protected Repository<Attribute> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public Iterator<Attribute> iterator()
	{
		Iterable<Attribute> attributeIterable = decoratedRepo::iterator;
		return StreamSupport.stream(attributeIterable.spliterator(), false).map(this::toPermittedAttribute).iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Attribute>> consumer, int batchSize)
	{
		MappedConsumer mappedConsumer = new MappedConsumer(consumer, this);
		decoratedRepo.forEachBatched(fetch, mappedConsumer::map, batchSize);
		super.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public Stream<Attribute> findAll(Query<Attribute> q)
	{
		return decoratedRepo.findAll(q).map(this::toPermittedAttribute);
	}

	@Override
	public Attribute findOne(Query<Attribute> q)
	{
		return toPermittedAttribute(decoratedRepo.findOne(q));
	}

	@Override
	public Attribute findOneById(Object id)
	{
		return toPermittedAttribute(decoratedRepo.findOneById(id));
	}

	@Override
	public Attribute findOneById(Object id, Fetch fetch)
	{
		return toPermittedAttribute(decoratedRepo.findOneById(id, fetch));
	}

	@Override
	public Stream<Attribute> findAll(Stream<Object> ids)
	{
		return decoratedRepo.findAll(ids).map(this::toPermittedAttribute);
	}

	@Override
	public Stream<Attribute> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepo.findAll(ids, fetch).map(this::toPermittedAttribute);
	}

	private Attribute toPermittedAttribute(Attribute attribute)
	{
		if (attribute != null && !SecurityUtils.currentUserIsSuOrSystem())
		{
			SecurityId securityId = SecurityId.create(SecurityUtils.getCurrentUsername(), SecurityId.Type.USER);
			if (entityAclService.isGranted(attribute, singletonList(Permission.READ), singletonList(securityId)))
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
