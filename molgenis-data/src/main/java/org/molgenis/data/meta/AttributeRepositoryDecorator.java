package org.molgenis.data.meta;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.util.EntityUtils;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserisSystem;

/**
 * Decorator for the attribute repository:
 * - filters requested entities based on the entity permissions of the current user.
 * - applies attribute metadata updates to the backend
 * <p>
 * TODO replace permission based entity filtering with generic row-level security once available
 */
public class AttributeRepositoryDecorator extends AbstractRepositoryDecorator<Attribute>
{
	private final Repository<Attribute> decoratedRepo;
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final DataService dataService;
	private final MolgenisPermissionService permissionService;

	public AttributeRepositoryDecorator(Repository<Attribute> decoratedRepo,
			SystemEntityTypeRegistry systemEntityTypeRegistry, DataService dataService,
			MolgenisPermissionService permissionService)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.dataService = requireNonNull(dataService);
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	protected Repository<Attribute> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public long count()
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.count();
		}
		else
		{
			Stream<Attribute> attrs = StreamSupport.stream(decoratedRepo.spliterator(), false);
			return filterCountPermission(attrs).count();
		}
	}

	@Override
	public long count(Query<Attribute> q)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.count(q);
		}
		else
		{
			// ignore query offset and page size
			Query<Attribute> qWithoutLimitOffset = new QueryImpl<>(q);
			qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
			Stream<Attribute> attrs = decoratedRepo.findAll(qWithoutLimitOffset);
			return filterCountPermission(attrs).count();
		}
	}

	@Override
	public Stream<Attribute> findAll(Query<Attribute> q)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.findAll(q);
		}
		else
		{
			Query<Attribute> qWithoutLimitOffset = new QueryImpl<>(q);
			qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
			Stream<Attribute> attrs = decoratedRepo.findAll(qWithoutLimitOffset);
			Stream<Attribute> filteredAttrs = filterReadPermission(attrs);
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

	@Override
	public Iterator<Attribute> iterator()
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.iterator();
		}
		else
		{
			Stream<Attribute> attrs = StreamSupport.stream(decoratedRepo.spliterator(), false);
			return filterReadPermission(attrs).iterator();
		}
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Attribute>> consumer, int batchSize)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			decoratedRepo.forEachBatched(fetch, consumer, batchSize);
		}
		else
		{
			FilteredConsumer filteredConsumer = new FilteredConsumer(consumer);
			decoratedRepo.forEachBatched(fetch, filteredConsumer::filter, batchSize);
		}
	}

	@Override
	public Attribute findOne(Query<Attribute> q)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.findOne(q);
		}
		else
		{
			// ignore query offset and page size
			return filterReadPermission(decoratedRepo.findOne(q));
		}
	}

	@Override
	public Attribute findOneById(Object id)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.findOneById(id);
		}
		else
		{
			return filterReadPermission(decoratedRepo.findOneById(id));
		}
	}

	@Override
	public Attribute findOneById(Object id, Fetch fetch)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.findOneById(id, fetch);
		}
		else
		{
			return filterReadPermission(decoratedRepo.findOneById(id, fetch));
		}
	}

	@Override
	public Stream<Attribute> findAll(Stream<Object> ids)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.findAll(ids);
		}
		else
		{
			return filterReadPermission(decoratedRepo.findAll(ids));
		}
	}

	@Override
	public Stream<Attribute> findAll(Stream<Object> ids, Fetch fetch)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.findAll(ids, fetch);
		}
		else
		{
			return filterReadPermission(decoratedRepo.findAll(ids, fetch));
		}
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.aggregate(aggregateQuery);
		}
		else
		{
			throw new MolgenisDataAccessException(format("Aggregation on entity [%s] not allowed", getName()));
		}
	}

	@Override
	public void update(Attribute attr)
	{
		validateUpdateAllowedAndUpdate(attr);
		decoratedRepo.update(attr);
	}

	@Override
	public void update(Stream<Attribute> attrs)
	{
		decoratedRepo.update(attrs.filter(attr ->
		{
			validateUpdateAllowedAndUpdate(attr);
			return true;
		}));
	}

	@Override
	public void delete(Attribute attr)
	{
		validateDeleteAllowed(attr);

		// If compound attribute is deleted then change the parent of children to null
		// This will change the children attributes into regular attributes.
		if (AttributeType.COMPOUND.equals(attr.getDataType()))
		{
			attr.getChildren().forEach(e ->
			{
				if (null != e.getParent())
				{
					dataService.getMeta().getRepository(AttributeMetadata.ATTRIBUTE_META_DATA)
							.update(e.setParent(null));
				}
			});
		}

		// remove this attribute
		decoratedRepo.delete(attr);
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
		decoratedRepo.add(attr);
	}

	@Override
	public Integer add(Stream<Attribute> attrs)
	{
		return decoratedRepo.add(attrs);
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

	/**
	 * Updates an attribute's representation in the backend for each concrete {@link EntityType} that
	 * has the {@link Attribute}.
	 *
	 * @param attr        current version of the attribute
	 * @param updatedAttr new version of the attribute
	 */
	private void updateAttributeInBackend(Attribute attr, Attribute updatedAttr)
	{
		MetaDataService meta = dataService.getMeta();
		meta.getConcreteChildren(attr.getEntity())
				.forEach(entityType -> meta.getBackend(entityType).updateAttribute(entityType, attr, updatedAttr));
	}

	private void validateUpdateAllowedAndUpdate(Attribute attr)
	{
		validateUpdateAllowed(attr);
		Attribute currentAttr = findOneById(attr.getIdentifier());
		updateAttributeInBackend(currentAttr, attr);
	}

	private Stream<Attribute> filterCountPermission(Stream<Attribute> attrs)
	{
		return filterPermission(attrs, COUNT);
	}

	private Attribute filterReadPermission(Attribute attr)
	{
		return attr != null ? filterReadPermission(Stream.of(attr)).findFirst().orElse(null) : null;
	}

	private Stream<Attribute> filterReadPermission(Stream<Attribute> attrs)
	{
		return filterPermission(attrs, READ);
	}

	private Stream<Attribute> filterPermission(Stream<Attribute> attrs, Permission permission)
	{
		return attrs.filter(attr -> permissionService.hasPermissionOnEntity(attr.getEntity().getId(), permission));
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
			Stream<Attribute> filteredAttrs = filterPermission(attrs.stream(), READ);
			consumer.accept(filteredAttrs.collect(toList()));
		}
	}
}
