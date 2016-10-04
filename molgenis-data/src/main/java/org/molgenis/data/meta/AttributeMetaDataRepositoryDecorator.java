package org.molgenis.data.meta;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.util.EntityUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.PARTS;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserisSystem;

/**
 * Decorator for the attribute meta data repository:
 * - filters requested entities based on the entity permissions of the current user.
 * - applies updates to the repository collection for attribute meta data adds/updates/deletes
 * <p>
 * TODO replace permission based entity filtering with generic row-level security once available
 */
public class AttributeMetaDataRepositoryDecorator implements Repository<Attribute>
{
	private final Repository<Attribute> decoratedRepo;
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;
	private final DataService dataService;
	private final MolgenisPermissionService permissionService;

	public AttributeMetaDataRepositoryDecorator(Repository<Attribute> decoratedRepo,
			SystemEntityMetaDataRegistry systemEntityMetaDataRegistry, DataService dataService,
			MolgenisPermissionService permissionService)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.systemEntityMetaDataRegistry = requireNonNull(systemEntityMetaDataRegistry);
		this.dataService = requireNonNull(dataService);
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepo.getCapabilities();
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepo.close();
	}

	@Override
	public String getName()
	{
		return decoratedRepo.getName();
	}

	@Override
	public Set<QueryRule.Operator> getQueryOperators()
	{
		return decoratedRepo.getQueryOperators();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepo.getEntityMetaData();
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
	public Query<Attribute> query()
	{
		return decoratedRepo.query();
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
		validateAndUpdate(attr);
		decoratedRepo.update(attr);
	}

	@Override
	public void update(Stream<Attribute> attrs)
	{
		decoratedRepo.update(attrs.filter(attr ->
		{
			validateAndUpdate(attr);
			return true;
		}));
	}

	@Override
	public void delete(Attribute attr)
	{
		validateDeleteAllowed(attr);
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
		validateDeleteAllowed(findOneById(id));
		decoratedRepo.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decoratedRepo.deleteAll(ids.filter(id ->
		{
			validateDeleteAllowed(findOneById(id));
			return true;
		}));
	}

	@Override
	public void deleteAll()
	{
		iterator().forEachRemaining(this::validateDeleteAllowed);
		decoratedRepo.deleteAll();
	}

	@Override
	public void add(Attribute attr)
	{
		// FIXME validate name
		decoratedRepo.add(attr);
	}

	@Override
	public Integer add(Stream<Attribute> attrs)
	{
		return decoratedRepo.add(attrs);
	}

	private void validateUpdate(Attribute currentAttr, Attribute newAttr)
	{
		// data type
		AttributeType currentDataType = currentAttr.getDataType();
		AttributeType newDataType = newAttr.getDataType();
		if (!Objects.equals(currentDataType, newDataType))
		{
			validateUpdateDataType(currentDataType, newDataType);
		}

		// expression
		String currentExpression = currentAttr.getExpression();
		String newExpression = newAttr.getExpression();
		if (!Objects.equals(currentExpression, newExpression))
		{
			validateUpdateExpression(currentExpression, newExpression);
		}

		String currentValidationExpression = currentAttr.getValidationExpression();
		String newValidationExpression = newAttr.getValidationExpression();
		if (!Objects.equals(currentValidationExpression, newValidationExpression))
		{
			validateUpdateExpression(currentValidationExpression, newValidationExpression);
		}

		String currentVisibleExpression = currentAttr.getVisibleExpression();
		String newVisibleExpression = newAttr.getVisibleExpression();
		if (!Objects.equals(currentVisibleExpression, newVisibleExpression))
		{
			validateUpdateExpression(currentVisibleExpression, newVisibleExpression);
		}
	}

	private static EnumMap<AttributeType, EnumSet<AttributeType>> DATA_TYPE_DISALLOWED_TRANSITIONS;

	static
	{
		// transitions to EMAIL and HYPERLINK not allowed because existing values not checked by PostgreSQL
		// transitions to CATEGORICAL_MREF and MREF not allowed because junction tables updated not implemented
		// transitions to FILE not allowed because associated file in FileStore not created/removed
		DATA_TYPE_DISALLOWED_TRANSITIONS = new EnumMap<>(AttributeType.class);
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(BOOL, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(CATEGORICAL, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(CATEGORICAL_MREF, EnumSet.complementOf(EnumSet.of(MREF)));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(COMPOUND, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(DATE, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(DATE_TIME, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(DECIMAL, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(EMAIL, EnumSet.of(CATEGORICAL_MREF, MREF, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(ENUM, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(FILE, EnumSet.allOf(AttributeType.class));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(HTML, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(HYPERLINK, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(INT, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(LONG, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(MREF, EnumSet.complementOf(EnumSet.of(CATEGORICAL_MREF)));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(SCRIPT, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(STRING, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(TEXT, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(XREF, EnumSet.of(CATEGORICAL_MREF, MREF, EMAIL, HYPERLINK, FILE));
	}

	private static void validateUpdateDataType(AttributeType currentDataType, AttributeType newDataType)
	{
		EnumSet<AttributeType> disallowedDataTypes = DATA_TYPE_DISALLOWED_TRANSITIONS.get(currentDataType);
		if (disallowedDataTypes.contains(newDataType))
		{
			throw new MolgenisDataException(
					format("Attribute data type update from [%s] to [%s] not allowed, allowed types are %s",
							currentDataType.toString(), newDataType.toString(),
							EnumSet.complementOf(disallowedDataTypes).toString()));
		}
	}

	private void validateUpdateExpression(String currentExpression, String newExpression)
	{
		// TODO validate with script evaluator
		// how to get access to expression validator here since it is located in molgenis-data-validation?
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
		Attribute systemAttr = systemEntityMetaDataRegistry.getSystemAttributeMetaData(attrIdentifier);
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
		if (systemEntityMetaDataRegistry.hasSystemAttributeMetaData(attrIdentifier))
		{
			throw new MolgenisDataException(
					format("Deleting system entity attribute [%s] is not allowed", attr.getName()));
		}
		EntityMetaData entityMeta = dataService.query(ENTITY_META_DATA, EntityMetaData.class).eq(ATTRIBUTES, attr)
				.findOne();
		if (entityMeta != null)
		{
			throw new MolgenisDataException(
					format("Deleting attribute [%s] is not allowed, since it is referenced by entity [%s]",
							attr.getName(), entityMeta.getName()));
		}
		Attribute attrMeta = dataService.query(ATTRIBUTE_META_DATA, Attribute.class).eq(PARTS, attr)
				.findOne();
		if (attrMeta != null)
		{
			throw new MolgenisDataException(
					format("Deleting attribute [%s] is not allowed, since it is referenced by attribute [%s]",
							attr.getName(), attrMeta.getName()));
		}
	}

	private void updateEntities(Attribute attr, Attribute updatedAttr)
	{
		getEntities(updatedAttr).forEach((entityMetaData) -> updateEntity(entityMetaData, attr, updatedAttr));
	}

	private void updateEntity(EntityMetaData entityMetaData, Attribute attr, Attribute updatedAttr)
	{
		dataService.getMeta().getBackend(entityMetaData.getBackend())
				.updateAttribute(entityMetaData, attr, updatedAttr);
	}

	/**
	 * Returns all entities that reference this attribute directly or via a compound attribute
	 *
	 * @param attr attribute
	 * @return entities referencing this attribute
	 */
	private Stream<EntityMetaData> getEntities(Attribute attr)
	{
		return getEntitiesRec(Collections.singletonList(attr));
	}

	private Stream<EntityMetaData> getEntitiesRec(List<Attribute> attrs)
	{
		// find entities referencing attributes
		Query<EntityMetaData> entityQ = dataService.query(ENTITY_META_DATA, EntityMetaData.class);
		Stream<EntityMetaData> entities;
		if (attrs.size() == 1)
		{
			entities = entityQ.eq(ATTRIBUTES, attrs.iterator().next()).findAll();
		}
		else
		{
			entities = entityQ.in(ATTRIBUTES, attrs).findAll();
		}

		// find attributes referencing attributes
		Query<Attribute> attrQ = dataService.query(ATTRIBUTE_META_DATA, Attribute.class);
		List<Attribute> parentAttrs;
		if (attrs.size() == 1)
		{
			parentAttrs = attrQ.eq(PARTS, attrs.iterator().next()).findAll().collect(toList());
		}
		else
		{
			parentAttrs = attrQ.in(PARTS, attrs).findAll().collect(toList());
		}

		// recurse for parent attributes
		if (!parentAttrs.isEmpty())
		{
			entities = Stream.concat(entities, getEntitiesRec(parentAttrs));
		}

		return entities;
	}

	private void validateAndUpdate(Attribute attr)
	{
		validateUpdateAllowed(attr);
		Attribute currentAttr = findOneById(attr.getIdentifier());
		validateUpdate(currentAttr, attr);
		updateEntities(currentAttr, attr);
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
		return attrs.filter(attr ->
		{
			Stream<EntityMetaData> entities = runAsSystem(() -> getEntities(attr));
			for (Iterator<EntityMetaData> it = entities.iterator(); it.hasNext(); )
			{
				if (permissionService.hasPermissionOnEntity(it.next().getName(), permission))
				{
					return true;
				}
			}
			return false;
		});
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
