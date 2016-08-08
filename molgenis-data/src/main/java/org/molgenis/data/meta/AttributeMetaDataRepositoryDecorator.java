package org.molgenis.data.meta;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
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
public class AttributeMetaDataRepositoryDecorator implements Repository<AttributeMetaData>
{
	private final Repository<AttributeMetaData> decoratedRepo;
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;
	private final DataService dataService;
	private final MolgenisPermissionService permissionService;

	public AttributeMetaDataRepositoryDecorator(Repository<AttributeMetaData> decoratedRepo,
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
			Stream<AttributeMetaData> attrs = StreamSupport.stream(decoratedRepo.spliterator(), false);
			return filterCountPermission(attrs).count();
		}
	}

	@Override
	public Query<AttributeMetaData> query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query<AttributeMetaData> q)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.count(q);
		}
		else
		{
			// ignore query offset and page size
			Query<AttributeMetaData> qWithoutLimitOffset = new QueryImpl<>(q);
			qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
			Stream<AttributeMetaData> attrs = decoratedRepo.findAll(qWithoutLimitOffset);
			return filterCountPermission(attrs).count();
		}
	}

	@Override
	public Stream<AttributeMetaData> findAll(Query<AttributeMetaData> q)
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.findAll(q);
		}
		else
		{
			Query<AttributeMetaData> qWithoutLimitOffset = new QueryImpl<>(q);
			qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
			Stream<AttributeMetaData> attrs = decoratedRepo.findAll(qWithoutLimitOffset);
			Stream<AttributeMetaData> filteredAttrs = filterReadPermission(attrs);
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
	public Iterator<AttributeMetaData> iterator()
	{
		if (currentUserIsSu() || currentUserisSystem())
		{
			return decoratedRepo.iterator();
		}
		else
		{
			Stream<AttributeMetaData> attrs = StreamSupport.stream(decoratedRepo.spliterator(), false);
			return filterReadPermission(attrs).iterator();
		}
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<AttributeMetaData>> consumer, int batchSize)
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
	public AttributeMetaData findOne(Query<AttributeMetaData> q)
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
	public AttributeMetaData findOneById(Object id)
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
	public AttributeMetaData findOneById(Object id, Fetch fetch)
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
	public Stream<AttributeMetaData> findAll(Stream<Object> ids)
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
	public Stream<AttributeMetaData> findAll(Stream<Object> ids, Fetch fetch)
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
	public void update(AttributeMetaData attr)
	{
		validateAndUpdate(attr);
		decoratedRepo.update(attr);
	}

	@Override
	public void update(Stream<AttributeMetaData> attrs)
	{
		decoratedRepo.update(attrs.filter(attr ->
		{
			validateAndUpdate(attr);
			return true;
		}));
	}

	@Override
	public void delete(AttributeMetaData attr)
	{
		validateAndDelete(attr);
		decoratedRepo.delete(attr);
	}

	@Override
	public void delete(Stream<AttributeMetaData> attrs)
	{
		decoratedRepo.delete(attrs.filter(attr ->
		{
			validateAndDelete(attr);
			return true;
		}));
	}

	@Override
	public void deleteById(Object id)
	{
		validateAndDelete(findOneById(id));
		decoratedRepo.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decoratedRepo.deleteById(ids.filter(id ->
		{
			validateAndDelete(findOneById(id));
			return true;
		}));
	}

	@Override
	public void deleteAll()
	{
		iterator().forEachRemaining(this::validateAndDelete);
		decoratedRepo.deleteAll();
	}

	@Override
	public void add(AttributeMetaData attr)
	{
		// FIXME validate name
		decoratedRepo.add(attr);
	}

	@Override
	public Integer add(Stream<AttributeMetaData> attrs)
	{
		return decoratedRepo.add(attrs);
	}

	private void validateUpdate(AttributeMetaData currentAttr, AttributeMetaData newAttr)
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

	private static EnumMap<AttributeType, EnumSet<AttributeType>> DATA_TYPE_ALLOWED_TRANSITIONS;

	static
	{
		DATA_TYPE_ALLOWED_TRANSITIONS = new EnumMap<>(AttributeType.class);
		DATA_TYPE_ALLOWED_TRANSITIONS.put(CATEGORICAL, EnumSet.of(XREF));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(CATEGORICAL_MREF, EnumSet.of(MREF));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(EMAIL, EnumSet.of(STRING));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(HTML, EnumSet.of(TEXT));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(HYPERLINK, EnumSet.of(STRING));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(MREF, EnumSet.of(CATEGORICAL_MREF));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(SCRIPT, EnumSet.of(TEXT));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(TEXT, EnumSet.of(SCRIPT));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(XREF, EnumSet.of(CATEGORICAL));
		// FIXME extend map
		DATA_TYPE_ALLOWED_TRANSITIONS.put(STRING, EnumSet.of(INT, DECIMAL));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(INT, EnumSet.of(STRING));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(DECIMAL, EnumSet.of(STRING));
	}

	private static void validateUpdateDataType(AttributeType currentDataType, AttributeType newDataType)
	{
		EnumSet<AttributeType> allowedDataTypes = DATA_TYPE_ALLOWED_TRANSITIONS.get(currentDataType);
		if (allowedDataTypes == null || !allowedDataTypes.contains(newDataType))
		{
			throw new MolgenisDataException(
					format("Attribute data type update from [%s] to [%s] not allowed", currentDataType.toString(),
							newDataType.toString()));
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
	private void validateUpdateAllowed(AttributeMetaData attr)
	{
		String attrIdentifier = attr.getIdentifier();
		AttributeMetaData systemAttr = systemEntityMetaDataRegistry.getSystemAttributeMetaData(attrIdentifier);
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
	private void validateDeleteAllowed(AttributeMetaData attr)
	{
		String attrIdentifier = attr.getIdentifier();
		if (systemEntityMetaDataRegistry.hasSystemAttributeMetaData(attrIdentifier))
		{
			throw new MolgenisDataException(
					format("Deleting system entity attribute [%s] is not allowed", attr.getName()));
		}
	}

	private void updateEntities(AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		getEntities(updatedAttr).forEach((entityMetaData) -> updateEntity(entityMetaData, attr, updatedAttr));
	}

	private void updateEntity(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
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
	private Stream<EntityMetaData> getEntities(AttributeMetaData attr)
	{
		return getEntitiesRec(Collections.singletonList(attr));
	}

	private Stream<EntityMetaData> getEntitiesRec(List<AttributeMetaData> attrs)
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
		Query<AttributeMetaData> attrQ = dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class);
		List<AttributeMetaData> parentAttrs;
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

	private void validateAndUpdate(AttributeMetaData attr)
	{
		validateUpdateAllowed(attr);
		AttributeMetaData currentAttr = findOneById(attr.getIdentifier());
		validateUpdate(currentAttr, attr);
		updateEntities(currentAttr, attr);
	}

	/**
	 * Deleting an attribute remove the attribute from entities with this attribute
	 *
	 * @param attr attribute to remove
	 */
	private void validateAndDelete(AttributeMetaData attr)
	{
		validateDeleteAllowed(attr);
		getEntities(attr).forEach(entityMetaData ->
		{
			entityMetaData.removeAttribute(attr);
			dataService.update(ENTITY_META_DATA, entityMetaData);
		});
	}

	private Stream<AttributeMetaData> filterCountPermission(Stream<AttributeMetaData> attrs)
	{
		return filterPermission(attrs, COUNT);
	}

	private AttributeMetaData filterReadPermission(AttributeMetaData attr)
	{
		return attr != null ? filterReadPermission(Stream.of(attr)).findFirst().orElse(null) : null;
	}

	private Stream<AttributeMetaData> filterReadPermission(Stream<AttributeMetaData> attrs)
	{
		return filterPermission(attrs, READ);
	}

	private Stream<AttributeMetaData> filterPermission(Stream<AttributeMetaData> attrs, Permission permission)
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
		private final Consumer<List<AttributeMetaData>> consumer;

		FilteredConsumer(Consumer<List<AttributeMetaData>> consumer)
		{
			this.consumer = requireNonNull(consumer);
		}

		public void filter(List<AttributeMetaData> attrs)
		{
			Stream<AttributeMetaData> filteredAttrs = filterPermission(attrs.stream(), READ);
			consumer.accept(filteredAttrs.collect(toList()));
		}
	}
}
