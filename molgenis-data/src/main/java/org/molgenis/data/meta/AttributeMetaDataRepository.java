package org.molgenis.data.meta;

import static java.util.Objects.requireNonNull;
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
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL;
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Range;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.fieldtypes.CompoundField;
import org.molgenis.fieldtypes.EnumField;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * Helper class around the {@link AttributeMetaDataMetaData} repository. Internal implementation class, use
 * {@link MetaDataServiceImpl} instead.
 */
class AttributeMetaDataRepository
{
	private static final int BATCH_SIZE = 1000;

	public static final AttributeMetaDataMetaData META_DATA = AttributeMetaDataMetaData.INSTANCE;

	private final UuidGenerator uuidGenerator;
	private final Repository repository;
	private EntityMetaDataRepository entityMetaDataRepository;
	private final LanguageService languageService;

	public AttributeMetaDataRepository(ManageableRepositoryCollection collection, LanguageService languageService)
	{
		this.repository = requireNonNull(collection).addEntityMeta(META_DATA);
		uuidGenerator = new UuidGenerator();
		this.languageService = languageService;
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
		return add(Arrays.asList(att)).iterator().next();
	}

	public Iterable<Entity> add(Iterable<AttributeMetaData> attrs)
	{
		Iterable<List<AttributeMetaData>> batches = Iterables.partition(attrs, BATCH_SIZE);
		return new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return stream(batches.spliterator(), false).flatMap(batch -> {
					List<Entity> attrEntities = convertToAttrEntities(batch);
					repository.add(attrEntities.stream());
					return attrEntities.stream();
				}).iterator();
			}

			private List<Entity> convertToAttrEntities(Iterable<AttributeMetaData> attrs)
			{
				return stream(attrs.spliterator(), false).map(this::convertToAttrEntity).collect(toList());
			}

			private Entity convertToAttrEntity(AttributeMetaData attr)
			{
				Entity attributeMetaDataEntity = new MapEntity(META_DATA);
				attributeMetaDataEntity.set(IDENTIFIER, uuidGenerator.generateId());
				attributeMetaDataEntity.set(NAME, attr.getName());
				attributeMetaDataEntity.set(DATA_TYPE, attr.getDataType());
				attributeMetaDataEntity.set(NILLABLE, attr.isNillable());
				attributeMetaDataEntity.set(AUTO, attr.isAuto());
				attributeMetaDataEntity.set(VISIBLE, attr.isVisible());
				attributeMetaDataEntity.set(LABEL, attr.getLabel());
				attributeMetaDataEntity.set(DESCRIPTION, attr.getDescription());
				attributeMetaDataEntity.set(AGGREGATEABLE, attr.isAggregateable());
				attributeMetaDataEntity.set(READ_ONLY, attr.isReadonly());
				attributeMetaDataEntity.set(UNIQUE, attr.isUnique());
				attributeMetaDataEntity.set(EXPRESSION, attr.getExpression());
				attributeMetaDataEntity.set(VISIBLE_EXPRESSION, attr.getVisibleExpression());
				attributeMetaDataEntity.set(VALIDATION_EXPRESSION, attr.getValidationExpression());
				attributeMetaDataEntity.set(DEFAULT_VALUE, attr.getDefaultValue());

				if ((attr.getDataType() instanceof EnumField) && (attr.getEnumOptions() != null))
				{
					attributeMetaDataEntity.set(ENUM_OPTIONS, Joiner.on(',').join(attr.getEnumOptions()));
				}

				if (attr.getRange() != null)
				{
					attributeMetaDataEntity.set(RANGE_MIN, attr.getRange().getMin());
					attributeMetaDataEntity.set(RANGE_MAX, attr.getRange().getMax());
				}

				if (attr.getRefEntity() != null)
				{
					String entityName = attr.getRefEntity().getName();
					attributeMetaDataEntity.set(REF_ENTITY, entityName);
				}

				// recursive for compound attribute parts
				if (attr.getDataType() instanceof CompoundField)
				{
					List<Entity> attrPartsEntities = convertToAttrEntities(attr.getAttributeParts());
					repository.add(attrPartsEntities.stream());
					attributeMetaDataEntity.set(PARTS, attrPartsEntities);
				}

				// Language attributes
				for (String languageCode : attr.getLabelLanguageCodes())
				{
					String attributeName = LABEL + '-' + languageCode;
					String label = attr.getLabel(languageCode);
					if (label != null) attributeMetaDataEntity.set(attributeName, label);
				}

				for (String languageCode : attr.getDescriptionLanguageCodes())
				{
					String attributeName = DESCRIPTION + '-' + languageCode;
					String description = attr.getDescription(languageCode);
					if (description != null) attributeMetaDataEntity.set(attributeName, description);
				}

				return attributeMetaDataEntity;
			}
		};
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
		attributeMetaData.setVisible(entity.getBoolean(VISIBLE));
		attributeMetaData.setLabel(entity.getString(LABEL));
		attributeMetaData.setDescription(entity.getString(DESCRIPTION));
		attributeMetaData
				.setAggregateable(entity.getBoolean(AGGREGATEABLE) == null ? false : entity.getBoolean(AGGREGATEABLE));
		attributeMetaData.setEnumOptions(entity.getList(ENUM_OPTIONS));
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
			stream(parts.spliterator(), false).map(this::toAttributeMetaData)
					.forEach(attributeMetaData::addAttributePart);
		}
		attributeMetaData.setVisibleExpression(entity.getString(VISIBLE_EXPRESSION));
		attributeMetaData.setValidationExpression(entity.getString(VALIDATION_EXPRESSION));
		attributeMetaData.setDefaultValue(entity.getString(DEFAULT_VALUE));

		// Language attributes
		for (String languageCode : languageService.getLanguageCodes())
		{
			String attributeName = LABEL + '-' + languageCode;
			String label = entity.getString(attributeName);
			if (label != null) attributeMetaData.setLabel(languageCode, label);

			attributeName = DESCRIPTION + '-' + languageCode;
			String description = entity.getString(attributeName);
			if (description != null) attributeMetaData.setDescription(languageCode, description);
		}

		return attributeMetaData;
	}

}