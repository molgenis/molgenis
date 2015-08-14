package org.molgenis.data.meta;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.AGGREGATEABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.AUTO;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DEFAULT_VALUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ENUM_OPTIONS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.EXPRESSION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.IDENTIFIER;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LOOKUP_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NAME;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NILLABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.PARTS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MAX;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MIN;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.READ_ONLY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.UNIQUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VALIDATION_EXPRESSION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE_EXPRESSION;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Range;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.fieldtypes.CompoundField;
import org.molgenis.fieldtypes.EnumField;

import com.google.common.base.Joiner;

/**
 * Helper class around the {@link AttributeMetaDataMetaData} repository. Internal implementation class, use
 * {@link MetaDataServiceImpl} instead.
 */
class AttributeMetaDataRepository
{
	public static final AttributeMetaDataMetaData META_DATA = AttributeMetaDataMetaData.INSTANCE;

	private final UuidGenerator uuidGenerator;

	private final Repository repository;

	private EntityMetaDataRepository entityMetaDataRepository;

	public AttributeMetaDataRepository(ManageableRepositoryCollection collection)
	{
		this.repository = collection.addEntityMeta(META_DATA);
		uuidGenerator = new UuidGenerator();
	}

	public void setEntityMetaDataRepository(EntityMetaDataRepository entityMetaDataRepository)
	{
		this.entityMetaDataRepository = entityMetaDataRepository;
	}

	Repository getRepository()
	{
		return repository;
	}

	/**
	 * Adds an attribute to the repository and returns the Entity it's created for it. If the attribute is a compound
	 * attribute with attribute parts, will also add all the parts.
	 * 
	 * @param att
	 *            AttributeMetaData to add
	 * @return the AttributeMetaDataMetaData entity that got created
	 */
	public Entity add(AttributeMetaData att)
	{
		Entity attributeMetaDataEntity = new MapEntity(META_DATA);
		// autoid
		attributeMetaDataEntity.set(IDENTIFIER, uuidGenerator.generateId());
		attributeMetaDataEntity.set(NAME, att.getName());
		attributeMetaDataEntity.set(DATA_TYPE, att.getDataType());
		attributeMetaDataEntity.set(ID_ATTRIBUTE, att.isIdAtrribute());
		attributeMetaDataEntity.set(NILLABLE, att.isNillable());
		attributeMetaDataEntity.set(AUTO, att.isAuto());
		attributeMetaDataEntity.set(VISIBLE, att.isVisible());
		attributeMetaDataEntity.set(LABEL, att.getLabel());
		attributeMetaDataEntity.set(DESCRIPTION, att.getDescription());
		attributeMetaDataEntity.set(AGGREGATEABLE, att.isAggregateable());
		attributeMetaDataEntity.set(LOOKUP_ATTRIBUTE, att.isLookupAttribute());
		attributeMetaDataEntity.set(LABEL_ATTRIBUTE, att.isLabelAttribute());
		attributeMetaDataEntity.set(READ_ONLY, att.isReadonly());
		attributeMetaDataEntity.set(UNIQUE, att.isUnique());
		attributeMetaDataEntity.set(EXPRESSION, att.getExpression());
		attributeMetaDataEntity.set(VISIBLE_EXPRESSION, att.getVisibleExpression());
		attributeMetaDataEntity.set(VALIDATION_EXPRESSION, att.getValidationExpression());
		attributeMetaDataEntity.set(DEFAULT_VALUE, att.getDefaultValue());

		if ((att.getDataType() instanceof EnumField) && (att.getEnumOptions() != null))
		{
			attributeMetaDataEntity.set(ENUM_OPTIONS, Joiner.on(",").join(att.getEnumOptions()));
		}

		if (att.getRange() != null)
		{
			attributeMetaDataEntity.set(RANGE_MIN, att.getRange().getMin());
			attributeMetaDataEntity.set(RANGE_MAX, att.getRange().getMax());
		}

		if (att.getRefEntity() != null)
		{
			String entityName = att.getRefEntity().getName();
			attributeMetaDataEntity.set(REF_ENTITY, entityName);
		}

		// recursive for compound attribute parts
		if (att.getDataType() instanceof CompoundField)
		{
			Iterable<AttributeMetaData> attributeParts = att.getAttributeParts();
			if (attributeParts != null)
			{
				attributeMetaDataEntity.set(PARTS,
						stream(attributeParts.spliterator(), false).map(this::add).collect(toList()));
			}
		}

		repository.add(attributeMetaDataEntity);
		return attributeMetaDataEntity;
	}

	/**
	 * Deletes attributes from the repository. If the attribute is a compound attribute with attribute parts, also
	 * deletes its parts.
	 * 
	 * @param attributes
	 *            Iterable<Entity> for the attribute that should be deleted
	 */
	public void deleteAttributes(Iterable<Entity> attributes)
	{
		if (attributes != null)
		{
			for (Entity attribute : attributes)
			{
				deleteAttributes(attribute.getEntities(PARTS));
				repository.delete(attribute);
			}
		}
	}

	/**
	 * Deletes all Attributes from the repository.
	 */
	public void deleteAll()
	{
		repository.deleteAll();
	}

	/**
	 * Creates a {@link DefaultAttributeMetaData} instance for an Entity in the repository.
	 * 
	 * @param entity
	 *            {@link AttributeMetaDataMetaData} Entity
	 * @return {@link DefaultAttributeMetaData}, with {@link DefaultAttributeMetaData#getRefEntity()} properly filled if
	 *         needed.
	 */
	public DefaultAttributeMetaData toAttributeMetaData(Entity entity)
	{
		DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(entity.getString(NAME));
		attributeMetaData.setDataType(MolgenisFieldTypes.getType(entity.getString(DATA_TYPE)));
		attributeMetaData.setNillable(entity.getBoolean(NILLABLE));
		attributeMetaData.setAuto(entity.getBoolean(AUTO));
		attributeMetaData.setIdAttribute(entity.getBoolean(ID_ATTRIBUTE));
		attributeMetaData.setLookupAttribute(entity.getBoolean(LOOKUP_ATTRIBUTE));
		attributeMetaData.setVisible(entity.getBoolean(VISIBLE));
		attributeMetaData.setLabel(entity.getString(LABEL));
		attributeMetaData.setDescription(entity.getString(DESCRIPTION));
		attributeMetaData.setAggregateable(entity.getBoolean(AGGREGATEABLE) == null ? false : entity
				.getBoolean(AGGREGATEABLE));
		attributeMetaData.setEnumOptions(entity.getList(ENUM_OPTIONS));
		attributeMetaData.setLabelAttribute(entity.getBoolean(LABEL_ATTRIBUTE) == null ? false : entity
				.getBoolean(LABEL_ATTRIBUTE));
		attributeMetaData.setReadOnly(entity.getBoolean(READ_ONLY) == null ? false : entity.getBoolean(READ_ONLY));
		attributeMetaData.setUnique(entity.getBoolean(UNIQUE) == null ? false : entity.getBoolean(UNIQUE));
		attributeMetaData.setExpression(entity.getString(EXPRESSION));

		Long rangeMin = entity.getLong(RANGE_MIN);
		Long rangeMax = entity.getLong(RANGE_MAX);
		if ((rangeMin != null) || (rangeMax != null))
		{
			attributeMetaData.setRange(new Range(rangeMin, rangeMax));
		}
		if (entity.get(REF_ENTITY) != null)
		{
			final String refEntityName = entity.getString(REF_ENTITY);
			attributeMetaData.setRefEntity(entityMetaDataRepository.get(refEntityName));
		}
		Iterable<Entity> parts = entity.getEntities(PARTS);
		if (parts != null)
		{
			stream(parts.spliterator(), false).map(this::toAttributeMetaData).forEach(
					attributeMetaData::addAttributePart);
		}
		attributeMetaData.setVisibleExpression(entity.getString(VISIBLE_EXPRESSION));
		attributeMetaData.setValidationExpression(entity.getString(VALIDATION_EXPRESSION));
		attributeMetaData.setDefaultValue(entity.getString(DEFAULT_VALUE));

		return attributeMetaData;
	}

}