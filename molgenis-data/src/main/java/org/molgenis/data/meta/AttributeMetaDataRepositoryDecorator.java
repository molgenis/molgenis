package org.molgenis.data.meta;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL_MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.EMAIL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.HTML;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.INT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.SCRIPT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.TEXT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;

import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.util.EntityUtils;

public class AttributeMetaDataRepositoryDecorator implements Repository<AttributeMetaData>
{
	private final Repository<AttributeMetaData> decoratedRepo;
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;
	private final DataService dataService;

	public AttributeMetaDataRepositoryDecorator(Repository<AttributeMetaData> decoratedRepo,
			SystemEntityMetaDataRegistry systemEntityMetaDataRegistry, DataService dataService)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.systemEntityMetaDataRegistry = requireNonNull(systemEntityMetaDataRegistry);
		this.dataService = requireNonNull(dataService);
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
		return decoratedRepo.count();
	}

	@Override
	public Query<AttributeMetaData> query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query<AttributeMetaData> q)
	{
		return decoratedRepo.count(q);
	}

	@Override
	public Stream<AttributeMetaData> findAll(Query<AttributeMetaData> q)
	{
		return decoratedRepo.findAll(q);
	}

	@Override
	public Iterator<AttributeMetaData> iterator()
	{
		return decoratedRepo.iterator();
	}

	@Override
	public Stream<AttributeMetaData> stream(Fetch fetch)
	{
		return decoratedRepo.stream(fetch);
	}

	@Override
	public AttributeMetaData findOne(Query<AttributeMetaData> q)
	{
		return decoratedRepo.findOne(q);
	}

	@Override
	public AttributeMetaData findOneById(Object id)
	{
		return decoratedRepo.findOneById(id);
	}

	@Override
	public AttributeMetaData findOneById(Object id, Fetch fetch)
	{
		return decoratedRepo.findOneById(id, fetch);
	}

	@Override
	public Stream<AttributeMetaData> findAll(Stream<Object> ids)
	{
		return decoratedRepo.findAll(ids);
	}

	@Override
	public Stream<AttributeMetaData> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepo.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepo.aggregate(aggregateQuery);
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
		decoratedRepo.update(attrs.filter(attr -> {
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
		decoratedRepo.delete(attrs.filter(attr -> {
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
		decoratedRepo.deleteById(ids.filter(id -> {
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

	@Override
	public void flush()
	{
		decoratedRepo.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepo.clearCache();
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepo.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepo.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepo.removeEntityListener(entityListener);
	}

	private void validateUpdate(AttributeMetaData currentAttr, AttributeMetaData newAttr)
	{
		// data type
		FieldType currentDataType = currentAttr.getDataType();
		FieldType newDataType = newAttr.getDataType();
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

	private static EnumMap<FieldTypeEnum, EnumSet<FieldTypeEnum>> DATA_TYPE_ALLOWED_TRANSITIONS;

	static
	{
		DATA_TYPE_ALLOWED_TRANSITIONS = new EnumMap<>(FieldTypeEnum.class);
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

	private static void validateUpdateDataType(FieldType currentDataTypeStr, FieldType newDataTypeStr)
	{
		FieldTypeEnum currentDataType = currentDataTypeStr.getEnumType();
		FieldTypeEnum newDataType = newDataTypeStr.getEnumType();
		EnumSet<FieldTypeEnum> allowedDataTypes = DATA_TYPE_ALLOWED_TRANSITIONS.get(currentDataType);
		if (allowedDataTypes == null || !allowedDataTypes.contains(newDataType))
		{
			throw new MolgenisDataException(
					format("Attribute data type update from [%s] to [%s] not allowed", currentDataTypeStr,
							newDataTypeStr));
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

	private Stream<EntityMetaData> getEntities(AttributeMetaData attr)
	{
		return dataService.query(ENTITY_META_DATA, EntityMetaData.class).eq(ATTRIBUTES, attr).findAll();
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
		getEntities(attr).forEach(entityMetaData -> {
			entityMetaData.removeAttribute(attr);
			dataService.update(ENTITY_META_DATA, entityMetaData);
		});
	}
}