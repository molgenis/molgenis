package org.molgenis.data.security.meta;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.security.core.UserPermissionEvaluator;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;

/**
 * Decorator for the attribute repository:
 * - filters requested entities based on the entity permissions of the current user.
 * <p>
 * TODO replace permission based entity filtering with generic row-level security once available
 */
public class AttributeRepositorySecurityDecorator extends AbstractRepositoryDecorator<Attribute>
{
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final UserPermissionEvaluator permissionService;

	public AttributeRepositorySecurityDecorator(Repository<Attribute> delegateRepository,
			SystemEntityTypeRegistry systemEntityTypeRegistry, UserPermissionEvaluator permissionService)
	{
		super(delegateRepository);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	public long count()
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().count();
		}
		else
		{
			Stream<Attribute> attrs = StreamSupport.stream(delegate().spliterator(), false);
			return filterCountPermission(attrs).count();
		}
	}

	@Override
	public long count(Query<Attribute> q)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().count(q);
		}
		else
		{
			// ignore query offset and page size
			Query<Attribute> qWithoutLimitOffset = new QueryImpl<>(q);
			qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
			Stream<Attribute> attrs = delegate().findAll(qWithoutLimitOffset);
			return filterCountPermission(attrs).count();
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public Stream<Attribute> findAll(Query<Attribute> q)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findAll(q);
		}
		else
		{
			Query<Attribute> qWithoutLimitOffset = new QueryImpl<>(q);
			qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
			Stream<Attribute> attrs = delegate().findAll(qWithoutLimitOffset);
			Stream<Attribute> filteredAttrs = filterCountPermission(attrs);
			if (q.getOffset() > 0)
			{
				filteredAttrs = filteredAttrs.skip(q.getOffset());
			}
			if (q.getPageSize() > 0)
			{
				filteredAttrs = filteredAttrs.limit(q.getPageSize());
			}
			return filteredAttrs;
		}

	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public Iterator<Attribute> iterator()
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().iterator();
		}
		else
		{
			Stream<Attribute> attrs = StreamSupport.stream(delegate().spliterator(), false);
			return filterCountPermission(attrs).iterator();
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Attribute>> consumer, int batchSize)
	{
		if (currentUserIsSuOrSystem())
		{
			delegate().forEachBatched(fetch, consumer, batchSize);
		}
		else
		{
			FilteredConsumer filteredConsumer = new FilteredConsumer(consumer);
			delegate().forEachBatched(fetch, filteredConsumer::filter, batchSize);
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public Attribute findOne(Query<Attribute> q)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findOne(q);
		}
		else
		{
			// ignore query offset and page size
			return filterReadPermission(delegate().findOne(q));
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public Attribute findOneById(Object id)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findOneById(id);
		}
		else
		{
			return filterReadPermission(delegate().findOneById(id));
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public Attribute findOneById(Object id, Fetch fetch)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findOneById(id, fetch);
		}
		else
		{
			return filterReadPermission(delegate().findOneById(id, fetch));
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public Stream<Attribute> findAll(Stream<Object> ids)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findAll(ids);
		}
		else
		{
			return filterCountPermission(delegate().findAll(ids));
		}
	}

	//Users with COUNT permission on an entity need to be able to READ the METAdata of this entity
	//see: https://github.com/molgenis/molgenis/issues/6383
	@Override
	public Stream<Attribute> findAll(Stream<Object> ids, Fetch fetch)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().findAll(ids, fetch);
		}
		else
		{
			return filterCountPermission(delegate().findAll(ids, fetch));
		}
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		if (currentUserIsSuOrSystem())
		{
			return delegate().aggregate(aggregateQuery);
		}
		else
		{
			throw new MolgenisDataAccessException(format("Aggregation on entity [%s] not allowed", getName()));
		}
	}

	@Override
	public void update(Attribute attr)
	{
		validateUpdateAllowed(attr);
		delegate().update(attr);
	}

	@Override
	public void update(Stream<Attribute> attrs)
	{
		delegate().update(attrs.filter(attr ->
		{
			validateUpdateAllowed(attr);
			return true;
		}));
	}

	@Override
	public void delete(Attribute attr)
	{
		validateDeleteAllowed(attr);
		delegate().delete(attr);
	}

	@Override
	public void delete(Stream<Attribute> attrs)
	{
		// The validateDeleteAllowed check if querying the table in which we are deleting. Since the decorated repo only
		// guarantees that the attributes are deleted after the operation completes we have to delete the attributes one
		// by one
		attrs.forEach(this::delete);
	}

	@Override
	public void deleteById(Object id)
	{
		Attribute attr = findOneById(id);
		delete(attr);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		delete(findAll(ids));
	}

	@Override
	public void deleteAll()
	{
		delete(this.query().findAll());
	}

	@Override
	public void add(Attribute attr)
	{
		delegate().add(attr);
	}

	@Override
	public Integer add(Stream<Attribute> attrs)
	{
		return delegate().add(attrs);
	}

	/**
	 * Updating attribute meta data is allowed for non-system attributes. For system attributes updating attribute meta
	 * data is only allowed if the meta data defined in Java differs from the meta data stored in the database (in other
	 * words the Java code was updated).
	 *
	 * @param attr attribute
	 */
	private void validateUpdateAllowed(Attribute attr)
	{
		String attrIdentifier = attr.getIdentifier();
		Attribute systemAttr = systemEntityTypeRegistry.getSystemAttribute(attrIdentifier);
		if (systemAttr != null && !EntityUtils.equals(attr, systemAttr))
		{
			throw new MolgenisDataException(
					format("Updating system entity attribute [%s] is not allowed", attr.getName()));
		}
	}

	/**
	 * Deleting attribute meta data is allowed for non-system attributes.
	 *
	 * @param attr attribute
	 */
	private void validateDeleteAllowed(Attribute attr)
	{
		String attrIdentifier = attr.getIdentifier();
		if (systemEntityTypeRegistry.hasSystemAttribute(attrIdentifier))
		{
			throw new MolgenisDataException(
					format("Deleting system entity attribute [%s] is not allowed", attr.getName()));
		}
	}

	private Stream<Attribute> filterCountPermission(Stream<Attribute> attrs)
	{
		return filterPermission(attrs, EntityTypePermission.COUNT);
	}

	private Attribute filterReadPermission(Attribute attr)
	{
		return attr != null ? filterCountPermission(Stream.of(attr)).findFirst().orElse(null) : null;
	}

	private Stream<Attribute> filterPermission(Stream<Attribute> attrs, EntityTypePermission permission)
	{
		return attrs.filter(
				attr -> permissionService.hasPermission(new EntityTypeIdentity(attr.getEntity().getId()), permission));
	}

	private class FilteredConsumer
	{
		private final Consumer<List<Attribute>> consumer;

		FilteredConsumer(Consumer<List<Attribute>> consumer)
		{
			this.consumer = requireNonNull(consumer);
		}

		public void filter(List<Attribute> attrs)
		{
			Stream<Attribute> filteredAttrs = filterPermission(attrs.stream(), EntityTypePermission.COUNT);
			consumer.accept(filteredAttrs.collect(toList()));
		}
	}
}
