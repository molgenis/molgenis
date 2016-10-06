package org.molgenis.data.meta;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
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
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetadata.PARTS;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_META_DATA;
import static org.molgenis.data.support.EntityTypeUtils.isSingleReferenceType;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserisSystem;

/**
 * Decorator for the attribute repository:
 * - filters requested entities based on the entity permissions of the current user.
 * - applies updates to the repository collection for attribute meta data adds/updates/deletes
 * <p>
 * TODO replace permission based entity filtering with generic row-level security once available
 */
public class AttributeRepositoryDecorator implements Repository<Attribute>
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

	public EntityType getEntityType()
	{
		return decoratedRepo.getEntityType();
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
		validateAdd(attr);
		decoratedRepo.add(attr);
	}

	@Override
	public Integer add(Stream<Attribute> attrs)
	{
		return decoratedRepo.add(attrs.filter(attr ->
		{
			this.validateAdd(attr);
			return true;
		}));
	}

	private void validateAdd(Attribute newAttr)
	{
		// mappedBy
		validateMappedBy(newAttr, newAttr.getMappedBy());
	}

	private void validateUpdate(Attribute currentAttr, Attribute newAttr)
	{
		// data type
		AttributeType currentDataType = currentAttr.getDataType();
		AttributeType newDataType = newAttr.getDataType();
		if (!Objects.equals(currentDataType, newDataType))
		{
			validateUpdateDataType(currentDataType, newDataType);

			if (newAttr.isInversedBy())
			{
				throw new MolgenisDataException(
						format("Attribute data type change not allowed for bidirectional attribute [%s]",
								newAttr.getName()));
			}
		}

		// expression
		String currentExpression = currentAttr.getExpression();
		String newExpression = newAttr.getExpression();
		if (!Objects.equals(currentExpression, newExpression))
		{
			validateUpdateExpression(currentExpression, newExpression);
		}

		// validation expression
		String currentValidationExpression = currentAttr.getValidationExpression();
		String newValidationExpression = newAttr.getValidationExpression();
		if (!Objects.equals(currentValidationExpression, newValidationExpression))
		{
			validateUpdateExpression(currentValidationExpression, newValidationExpression);
		}

		// visible expression
		String currentVisibleExpression = currentAttr.getVisibleExpression();
		String newVisibleExpression = newAttr.getVisibleExpression();
		if (!Objects.equals(currentVisibleExpression, newVisibleExpression))
		{
			validateUpdateExpression(currentVisibleExpression, newVisibleExpression);
		}

		// note: mappedBy is a readOnly attribute, no need to verify for updates
	}

	private static EnumMap<AttributeType, EnumSet<AttributeType>> DATA_TYPE_DISALLOWED_TRANSITIONS;

	static
	{
		// transitions to EMAIL and HYPERLINK not allowed because existing values not checked by PostgreSQL
		// transitions to CATEGORICAL_MREF and MREF not allowed because junction tables updated not implemented
		// transitions to FILE not allowed because associated file in FileStore not created/removed
		DATA_TYPE_DISALLOWED_TRANSITIONS = new EnumMap<>(AttributeType.class);
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(BOOL, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(CATEGORICAL, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(CATEGORICAL_MREF, EnumSet.complementOf(EnumSet.of(MREF)));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(COMPOUND, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(DATE, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(DATE_TIME, EnumSet.of(CATEGORICAL_MREF, ONE_TO_MANY, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(DECIMAL, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(EMAIL, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(ENUM, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(FILE, EnumSet.allOf(AttributeType.class));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(HTML, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(HYPERLINK, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(INT, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(LONG, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(MREF, EnumSet.complementOf(EnumSet.of(CATEGORICAL_MREF)));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(ONE_TO_MANY, EnumSet.allOf(AttributeType.class));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(SCRIPT, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(STRING, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(TEXT, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(XREF, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
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
	 * Validate whether the mappedBy attribute is part of the referenced entity.
	 *
	 * @param attr         attribute
	 * @param mappedByAttr mappedBy attribute
	 * @throws MolgenisDataException if mappedBy is an attribute that is not part of the referenced entity
	 */
	private static void validateMappedBy(Attribute attr, Attribute mappedByAttr)
	{
		if (mappedByAttr != null)
		{
			if (!isSingleReferenceType(mappedByAttr))
			{
				throw new MolgenisDataException(
						format("Invalid mappedBy attribute [%s] data type [%s].", mappedByAttr.getName(),
								mappedByAttr.getDataType()));
			}

			Attribute refAttr = attr.getRefEntity().getAttribute(mappedByAttr.getName());
			if (refAttr == null)
			{
				throw new MolgenisDataException(
						format("mappedBy attribute [%s] is not part of entity [%s].", mappedByAttr.getName(),
								attr.getRefEntity().getName()));
			}
		}
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
		EntityType entityType = dataService.query(ENTITY_META_DATA, EntityType.class).eq(ATTRIBUTES, attr).findOne();
		if (entityType != null)
		{
			throw new MolgenisDataException(
					format("Deleting attribute [%s] is not allowed, since it is referenced by entity [%s]",
							attr.getName(), entityType.getName()));
		}
		Attribute attrMeta = dataService.query(ATTRIBUTE_META_DATA, Attribute.class).eq(PARTS, attr).findOne();
		if (attrMeta != null)
		{
			throw new MolgenisDataException(
					format("Deleting attribute [%s] is not allowed, since it is referenced by attribute [%s]",
							attr.getName(), attrMeta.getName()));
		}
	}

	private void updateEntities(Attribute attr, Attribute updatedAttr)
	{
		getEntities(updatedAttr).forEach((EntityType) -> updateEntity(EntityType, attr, updatedAttr));
	}

	private void updateEntity(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		dataService.getMeta().getBackend(entityType.getBackend()).updateAttribute(entityType, attr, updatedAttr);
	}

	/**
	 * Returns all entities that reference this attribute directly or via a compound attribute
	 *
	 * @param attr attribute
	 * @return entities referencing this attribute
	 */
	private Stream<EntityType> getEntities(Attribute attr)
	{
		return getEntitiesRec(Collections.singletonList(attr));
	}

	private Stream<EntityType> getEntitiesRec(List<Attribute> attrs)
	{
		// find entities referencing attributes
		Query<EntityType> entityQ = dataService.query(ENTITY_META_DATA, EntityType.class);
		Stream<EntityType> entities;
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
			Stream<EntityType> entities = runAsSystem(() -> getEntities(attr));
			for (Iterator<EntityType> it = entities.iterator(); it.hasNext(); )
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
