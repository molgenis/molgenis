package org.molgenis.data.security;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.security.acl.EntityAclManager;
import org.molgenis.data.security.acl.EntityIdentity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.Permission;
import org.molgenis.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;

/**
 * TODO Deleting entity type should delete ACLs (RepositoryCollectionDecorator?)
 * TODO decide on behavior of deleteAll: delete all entities that this user can retrieve via findAll or try to delete all entities of this entity type
 * TODO decide on behavior of update with regard to ref entities
 * TODO createCurrentUserSids is expensive, is this a performance bottleneck?
 */
public class EntitySecurityRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final EntityAclManager entityAclManager;

	public EntitySecurityRepositoryDecorator(Repository<Entity> delegateRepository, EntityAclManager entityAclManager)
	{
		super(delegateRepository);
		this.entityAclManager = requireNonNull(entityAclManager);
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		FilteredConsumer filteredConsumer = new FilteredConsumer(consumer, this);
		delegate().forEachBatched(fetch, filteredConsumer::filter, batchSize);
	}

	@Override
	public long count()
	{
		if (currentUserIsSuOrSystem())
		{
			return super.count();
		}
		// FIXME this requires READ row permissions instead of count
		return findAll(query()).count();
	}

	@Override
	public long count(Query<Entity> q)
	{
		if (currentUserIsSuOrSystem())
		{
			return super.count(q);
		}
		return findAll(q).count();
	}

	@Override
	public void add(Entity entity)
	{
		// current user is allowed to add entity, see RepositorySecurityDecorator
		entityAclManager.createAcl(entity);
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		// current user is allowed to add entities, see RepositorySecurityDecorator
		return delegate().add(entities.filter(entity ->
		{
			entityAclManager.createAcl(entity);
			return true;
		}));
	}

	// TODO update of xrefs mrefs for entity types that are row level secured
	@Override
	public void update(Entity entity)
	{
		validateCurrentUserCanUpdateEntity(entity);
		delegate().update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		delegate().update(entities.filter(entity ->
		{
			validateCurrentUserCanUpdateEntity(entity);
			return true;
		}));
	}

	@Override
	public void delete(Entity entity)
	{
		validateCurrentUserCanDeleteEntity(entity);
		deleteAcl(entity);
		delegate().delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		validateCurrentUserCanDeleteEntityById(id);
		deleteAcl(id);
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		if (currentUserIsSuOrSystem())
		{
			query().findAll().forEach(this::deleteAcl);
			delegate().deleteAll();
		}
		else
		{
			// delete all entities that the current user can read
			delete(query().findAll());
		}
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		delegate().delete(entities.filter(entity ->
		{
			validateCurrentUserCanDeleteEntity(entity);
			deleteAcl(entity);
			return true;
		}));
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		delegate().deleteAll(ids.filter(id ->
		{
			validateCurrentUserCanDeleteEntityById(id);
			deleteAcl(id);
			return true;
		}));
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findAll(ids);
		}

		return delegate().findAll(ids).filter(this::currentUserCanReadEntity);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findAll(ids, fetch);
		}

		return delegate().findAll(ids, fetch).filter(this::currentUserCanReadEntity);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findAll(q);
		}

		Query<Entity> qWithoutLimitOffset = new QueryImpl<>(q);
		qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
		Stream<Entity> entityStream = delegate().findAll(qWithoutLimitOffset).filter(this::currentUserCanReadEntity);
		if (q.getOffset() > 0)
		{
			entityStream = entityStream.skip(q.getOffset());
		}
		if (q.getPageSize() > 0)
		{
			entityStream = entityStream.limit(q.getPageSize());
		}
		return entityStream;
	}

	// TODO we do not use Elasticsearch aggregates here
	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().aggregate(aggregateQuery);
		}

		if (aggregateQuery.getAttributeDistinct() != null)
		{
			throw new UnsupportedOperationException("not implemented");
		}

		Query<Entity> query = aggregateQuery.getQuery();
		Stream<Entity> entityStream = query != null ? findAll(query) : findAll(query());

		Attribute attributeX = aggregateQuery.getAttributeX();
		Attribute attributeY = aggregateQuery.getAttributeY();

		List<List<Long>> matrix;
		List<Object> xLabels, yLabels;
		if (attributeY == null)
		{
			Map<Object, Long> countMap = entityStream.map(entity -> entity.get(attributeX.getName()))
													 .collect(Collectors.groupingBy(Function.identity(),
															 Collectors.counting()));

			xLabels = newArrayList(countMap.keySet());
			yLabels = emptyList();
			matrix = singletonList(newArrayList(countMap.values()));
		}
		else
		{
			Map<Pair<Object, Object>, Long> countMap = entityStream.map(
					entity -> new Pair<>(entity.get(attributeX.getName()), entity.get(attributeY.getName())))
																   .collect(Collectors.groupingBy(Function.identity(),
																		   Collectors.counting()));

			xLabels = newArrayList(countMap.keySet().stream().map(Pair::getA).collect(toSet()));
			yLabels = newArrayList(countMap.keySet().stream().map(Pair::getB).collect(toSet()));
			matrix = new ArrayList<>(xLabels.size());
			for (int i = 0; i < xLabels.size(); ++i)
			{
				ArrayList<Long> yValues = new ArrayList<>(yLabels.size());
				for (int y = 0; y < yLabels.size(); ++y)
				{
					yValues.add(0L);
				}
				matrix.add(yValues);
			}
			countMap.forEach((pair, count) ->
			{
				int xIndex = xLabels.indexOf(pair.getA());
				int yIndex = yLabels.indexOf(pair.getB());
				matrix.get(xIndex).set(yIndex, count);
			});
		}
		return new AggregateResult(matrix, xLabels, yLabels);
	}

	private boolean currentUserCanReadEntity(Entity entity)
	{
		return currentUserIsSuOrSystem() || currentUserCanAccessEntity(entity, Permission.READ);
	}

	private void validateCurrentUserCanUpdateEntity(Entity entity)
	{
		if (currentUserIsSuOrSystem())
		{
			return;
		}
		validateCurrentUserCanAccessEntity(entity, Permission.WRITE);
	}

	private void validateCurrentUserCanDeleteEntity(Entity entity)
	{
		if (currentUserIsSuOrSystem())
		{
			return;
		}
		validateCurrentUserCanAccessEntity(entity, Permission.WRITE);
	}

	private void validateCurrentUserCanDeleteEntityById(Object entityId)
	{
		if (currentUserIsSuOrSystem())
		{
			return;
		}

		Entity entity = findOneById(entityId);
		if (entity == null)
		{
			String entityIdStr = entityId.toString();
			String entityTypeIdStr = getEntityType().getIdValue().toString();
			throw new UnknownEntityException(format("Unknown entity [%s] of type [%s]", entityIdStr, entityTypeIdStr));
		}

		validateCurrentUserCanAccessEntity(entity, Permission.WRITE);
	}

	private boolean currentUserCanAccessEntity(Entity entity, Permission permission)
	{
		EntityIdentity entityIdentity = EntityIdentity.create(entity.getEntityType().getId(), entity.getIdValue());
		return entityAclManager.isGranted(entityIdentity, permission);
	}

	private void validateCurrentUserCanAccessEntity(Entity entity, Permission permission)
	{
		boolean isGranted = currentUserCanAccessEntity(entity, permission);
		if (!isGranted)
		{
			String entityIdStr = entity.getIdValue().toString();
			String entityTypeIdStr = getEntityType().getIdValue().toString();
			throw new MolgenisDataAccessException(
					format("Updating entity [%s] of type [%s] is not allowed", entityIdStr, entityTypeIdStr));
		}
	}

	private void deleteAcl(Entity entity)
	{
		EntityIdentity entityIdentity = EntityIdentity.create(entity.getEntityType().getId(), entity.getIdValue());
		entityAclManager.deleteAcl(entityIdentity);
	}

	private void deleteAcl(Object entityId)
	{
		EntityIdentity entityIdentity = EntityIdentity.create(getEntityType().getId(), entityId);
		entityAclManager.deleteAcl(entityIdentity);
	}

	private class FilteredConsumer
	{
		private final Consumer<List<Entity>> consumer;
		private final EntitySecurityRepositoryDecorator entitySecurityRepositoryDecorator;

		FilteredConsumer(Consumer<List<Entity>> consumer,
				EntitySecurityRepositoryDecorator entitySecurityRepositoryDecorator)
		{
			this.consumer = requireNonNull(consumer);
			this.entitySecurityRepositoryDecorator = requireNonNull(entitySecurityRepositoryDecorator);
		}

		public void filter(List<Entity> entities)
		{
			Stream<Entity> filteredEntities = entities.stream()
													  .filter(entitySecurityRepositoryDecorator::currentUserCanReadEntity);
			consumer.accept(filteredEntities.collect(toList()));
		}
	}
}