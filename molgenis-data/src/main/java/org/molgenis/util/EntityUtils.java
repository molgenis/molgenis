package org.molgenis.util;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.Package;
import org.molgenis.data.meta.Tag;
import org.springframework.beans.BeanUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class EntityUtils
{

	/**
	 * Checks if an entity contains data or not
	 *
	 * @param entity
	 */
	public static boolean isEmpty(Entity entity)
	{
		for (String attr : entity.getAttributeNames())
		{
			if (entity.get(attr) != null)
			{
				return false;
			}
		}

		return true;
	}

	public static List<Pair<EntityMetaData, List<AttributeMetaData>>> getReferencingEntityMetaData(
			EntityMetaData entityMetaData, DataService dataService)
	{
		List<Pair<EntityMetaData, List<AttributeMetaData>>> referencingEntityMetaData = new ArrayList<Pair<EntityMetaData, List<AttributeMetaData>>>();

		// get entity types that referencing the given entity (including self)
		String entityName = entityMetaData.getName();
		dataService.getEntityNames().forEach(otherEntityName -> {
			EntityMetaData otherEntityMetaData = dataService.getEntityMetaData(otherEntityName);

			// get referencing attributes for other entity
			List<AttributeMetaData> referencingAttributes = null;
			for (AttributeMetaData attributeMetaData : otherEntityMetaData.getAtomicAttributes())
			{
				EntityMetaData refEntityMetaData = attributeMetaData.getRefEntity();
				if (refEntityMetaData != null && refEntityMetaData.getName().equals(entityName))
				{
					if (referencingAttributes == null) referencingAttributes = new ArrayList<AttributeMetaData>();
					referencingAttributes.add(attributeMetaData);
				}
			}

			// store references
			if (referencingAttributes != null)
			{
				referencingEntityMetaData.add(new Pair<>(otherEntityMetaData, referencingAttributes));
			}
		});

		return referencingEntityMetaData;
	}

	/**
	 * Gets all attribute names of an EntityMetaData (atomic + compound)
	 *
	 * @param entityMetaData
	 * @return
	 */
	public static Iterable<String> getAttributeNames(EntityMetaData entityMetaData)
	{
		// atomic
		Iterable<String> atomicAttributes = Iterables
				.transform(entityMetaData.getAtomicAttributes(), new Function<AttributeMetaData, String>()
				{

					@Override
					public String apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getName();
					}
				});

		// compound
		Iterable<String> compoundAttributes = Iterables
				.transform(Iterables.filter(entityMetaData.getAttributes(), new Predicate<AttributeMetaData>()
				{
					@Override
					public boolean apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getDataType().getEnumType() == FieldTypeEnum.COMPOUND;
					}
				}), new Function<AttributeMetaData, String>()
				{
					@Override
					public String apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getName();
					}
				});

		// all = atomic + compound
		return Iterables.concat(atomicAttributes, compoundAttributes);
	}

	/**
	 * Convert a untyped entity to a typed entity.
	 * <p>
	 * If the entity is of the correct class it simply returns it. If entity of other class it tries to convert it, the
	 * entity class should have a constructor with dataservice as single arg or a arg less constructor
	 *
	 * @param entity
	 * @param entityClass
	 * @param dataService
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E convert(Entity entity, Class<E> entityClass, DataService dataService)
	{
		if (entityClass.isAssignableFrom(entity.getClass())) return (E) entity;
		E convertedEntity;

		// Find constructor with DataService as single arg
		Constructor<E> ctor = ConstructorUtils.getAccessibleConstructor(entityClass, Entity.class);
		if (ctor != null)
		{
			convertedEntity = BeanUtils.instantiateClass(ctor, entity);
		}
		else
		{
			ctor = ConstructorUtils.getAccessibleConstructor(entityClass, DataService.class);
			if (ctor != null)
			{
				convertedEntity = BeanUtils.instantiateClass(ctor, dataService);
			}
			else
			{
				// Find arg less constructor
				ctor = ConstructorUtils.getAccessibleConstructor(entityClass);
				if (ctor == null) throw new RuntimeException(
						"No usable constructor found for entity class [" + entityClass.getName()
								+ "]. Entity class should have a constructor with dataservice as single arg or a constructor without arguments");

				convertedEntity = BeanUtils.instantiateClass(ctor);
			}
			convertedEntity.set(entity);
		}

		return convertedEntity;
	}

	/**
	 * Checks if an entity has another entity as one of its parents
	 *
	 * @param entityMetaData
	 * @param entityName
	 * @return
	 */
	public static boolean doesExtend(EntityMetaData entityMetaData, String entityName)
	{
		EntityMetaData parent = entityMetaData.getExtends();
		while (parent != null)
		{
			if (parent.getName().equalsIgnoreCase(entityName)) return true;
			parent = parent.getExtends();
		}

		return false;
	}

	/**
	 * Get an Iterable of entities as a stream of entities
	 *
	 * @param entities
	 * @return
	 */
	public static Stream<Entity> asStream(Iterable<Entity> entities)
	{
		return StreamSupport.stream(entities.spliterator(), false);
	}

	/**
	 * Returns true if entity equals another entity. TODO docs
	 *
	 * @param entityMeta
	 * @param otherEntityMeta
	 * @return
	 */
	public static boolean equals(EntityMetaData entityMeta, EntityMetaData otherEntityMeta)
	{
		if (entityMeta == null && otherEntityMeta != null)
		{
			return false;
		}
		if (entityMeta != null && otherEntityMeta == null)
		{
			return false;
		}
		if (!entityMeta.getName().equals(otherEntityMeta.getName()))
		{
			return false;
		}
		if (!entityMeta.getSimpleName().equals(otherEntityMeta.getSimpleName()))
		{
			return false;
		}
		if (!Objects.equals(entityMeta.getLabel(), otherEntityMeta.getLabel()))
		{
			return false;
		}
		if (!Objects.equals(entityMeta.getDescription(), otherEntityMeta.getDescription()))
		{
			return false;
		}
		if (!entityMeta.getBackend().equals(otherEntityMeta.getBackend()))
		{
			return false;
		}

		// compare package identifiers
		Package package_ = entityMeta.getPackage();
		Package otherPackage = otherEntityMeta.getPackage();
		if (package_ == null && otherPackage != null)
		{
			return false;
		}
		if (package_ != null && otherPackage == null)
		{
			return false;
		}
		if (!package_.getIdValue().equals(otherPackage.getIdValue()))
		{
			return false;
		}

		// compare id attribute identifier (identifier might be null if id attribute hasn't been persisted yet)
		AttributeMetaData ownIdAttribute = entityMeta.getOwnIdAttribute();
		AttributeMetaData otherOwnIdAttribute = otherEntityMeta.getOwnIdAttribute();
		if (ownIdAttribute == null && otherOwnIdAttribute != null)
		{
			return false;
		}
		if (ownIdAttribute != null && otherOwnIdAttribute == null)
		{
			return false;
		}
		if (ownIdAttribute != null && otherOwnIdAttribute != null && !Objects
				.equals(ownIdAttribute.getIdentifier(), otherOwnIdAttribute.getIdentifier()))
		{
			return false;
		}

		// compare label attribute identifier (identifier might be null if id attribute hasn't been persisted yet)
		AttributeMetaData ownLabelAttribute = entityMeta.getOwnLabelAttribute();
		AttributeMetaData otherOwnLabelAttribute = otherEntityMeta.getOwnLabelAttribute();
		if (ownLabelAttribute == null && otherOwnLabelAttribute != null)
		{
			return false;
		}
		if (ownLabelAttribute != null && otherOwnLabelAttribute == null)
		{
			return false;
		}
		if (ownLabelAttribute != null && otherOwnLabelAttribute != null && !Objects
				.equals(ownLabelAttribute.getIdentifier(), otherOwnLabelAttribute.getIdentifier()))
		{
			return false;
		}

		// compare lookup attribute identifiers
		List<AttributeMetaData> lookupAttrs = newArrayList(entityMeta.getOwnLookupAttributes());
		List<AttributeMetaData> otherLookupAttrs = newArrayList(otherEntityMeta.getOwnLookupAttributes());
		if (lookupAttrs.size() != otherLookupAttrs.size())
		{
			return false;
		}
		for (int i = 0; i < lookupAttrs.size(); ++i)
		{
			// identifier might be null if id attribute hasn't been persisted yet
			if (!Objects.equals(lookupAttrs.get(i).getIdentifier(), otherLookupAttrs.get(i).getIdentifier()))
			{
				return false;
			}
		}

		if (entityMeta.isAbstract() != otherEntityMeta.isAbstract())
		{
			return false;
		}

		// compare extends entity identifier
		EntityMetaData extendsEntityMeta = entityMeta.getExtends();
		EntityMetaData otherExtendsEntityMeta = otherEntityMeta.getExtends();
		if (extendsEntityMeta == null && otherExtendsEntityMeta != null)
		{
			return false;
		}
		if (extendsEntityMeta != null && otherExtendsEntityMeta == null)
		{
			return false;
		}
		if (extendsEntityMeta != null && otherExtendsEntityMeta != null && !extendsEntityMeta.getName()
				.equals(otherExtendsEntityMeta.getName()))
		{
			return false;
		}

		// compare attributes
		if (!equals(entityMeta.getOwnAttributes(), otherEntityMeta.getOwnAttributes()))
		{
			return false;
		}

		// compare tag identifiers
		List<Tag> tags = newArrayList(entityMeta.getTags());
		List<Tag> otherTags = newArrayList(otherEntityMeta.getTags());
		if (tags.size() != otherTags.size())
		{
			return false;
		}
		for (int i = 0; i < tags.size(); ++i)
		{
			if (!tags.get(i).getIdentifier().equals(otherTags.get(i).getIdentifier()))
			{
				return false;
			}
		}

		return true;
	}

	public static boolean equals(Iterable<AttributeMetaData> attrsIt, Iterable<AttributeMetaData> otherAttrsIt)
	{
		List<AttributeMetaData> attrs = newArrayList(attrsIt);
		List<AttributeMetaData> otherAttrs = newArrayList(otherAttrsIt);

		if (attrs.size() != otherAttrs.size())
		{
			return false;
		}

		for (int i = 0; i < attrs.size(); ++i)
		{
			if (!equals(attrs.get(i), otherAttrs.get(i)))
			{
				return false;
			}
		}

		return true;
	}

	public static boolean equals(AttributeMetaData attr, AttributeMetaData otherAttr)
	{
		// identifier might be null if attribute hasn't been persisted yet
		if (!Objects.equals(attr.getIdentifier(), otherAttr.getIdentifier()))
		{
			return false;
		}

		if (!attr.getName().equals(otherAttr.getName()))
		{
			return false;
		}

		if (!attr.getLabel().equals(otherAttr.getLabel()))
		{
			return false;
		}

		if (!Objects.equals(attr.getDescription(), otherAttr.getDescription()))
		{
			return false;
		}

		if (!attr.getDataType().equals(otherAttr.getDataType()))
		{
			return false;
		}

		// recursively compare attribute parts
		if (!equals(attr.getAttributeParts(), otherAttr.getAttributeParts()))
		{
			return false;
		}

		// compare entity identifier
		EntityMetaData refEntity = attr.getRefEntity();
		EntityMetaData otherRefEntity = otherAttr.getRefEntity();
		if (refEntity == null && otherRefEntity != null)
		{
			return false;
		}
		if (refEntity != null && otherRefEntity == null)
		{
			return false;
		}
		if (refEntity != null && otherRefEntity != null && !refEntity.getName().equals(otherRefEntity.getName()))
		{
			return false;
		}

		if (!Objects.equals(attr.getExpression(), otherAttr.getExpression()))
		{
			return false;
		}

		if (attr.isNillable() != otherAttr.isNillable())
		{
			return false;
		}

		if (attr.isAuto() != otherAttr.isAuto())
		{
			return false;
		}

		if (attr.isVisible() != otherAttr.isVisible())
		{
			return false;
		}

		if (attr.isAggregatable() != otherAttr.isAggregatable())
		{
			return false;
		}

		if (!attr.getEnumOptions().equals(otherAttr.getEnumOptions()))
		{
			return false;
		}

		if (!Objects.equals(attr.getRangeMin(), otherAttr.getRangeMin()))
		{
			return false;
		}

		if (!Objects.equals(attr.getRangeMax(), otherAttr.getRangeMax()))
		{
			return false;
		}

		if (attr.isReadOnly() != otherAttr.isReadOnly())
		{
			return false;
		}

		if (attr.isUnique() != otherAttr.isUnique())
		{
			return false;
		}

		if (!Objects.equals(attr.getVisibleExpression(), otherAttr.getVisibleExpression()))
		{
			return false;
		}

		if (!Objects.equals(attr.getValidationExpression(), otherAttr.getValidationExpression()))
		{
			return false;
		}

		if (!Objects.equals(attr.getDefaultValue(), otherAttr.getDefaultValue()))
		{
			return false;
		}

		// compare tag identifiers
		List<Tag> tags = newArrayList(attr.getTags());
		List<Tag> otherTags = newArrayList(otherAttr.getTags());
		if (tags.size() != otherTags.size())
		{
			return false;
		}
		for (int i = 0; i < tags.size(); ++i)
		{
			if (!tags.get(i).getIdentifier().equals(otherTags.get(i).getIdentifier()))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns true if entity equals another entity. For referenced entities compares the referenced entity ids.
	 *
	 * @param entity
	 * @param otherEntity
	 * @return true if entity equals another entity
	 */
	public static boolean equals(Entity entity, Entity otherEntity)
	{
		if (entity == null && otherEntity != null)
		{
			return false;
		}
		if (entity != null && otherEntity == null)
		{
			return false;
		}
		if (!entity.getEntityMetaData().getName().equals(otherEntity.getEntityMetaData().getName()))
		{
			return false;
		}
		for (AttributeMetaData attr : entity.getEntityMetaData().getAtomicAttributes())
		{
			String attrName = attr.getName();
			switch (attr.getDataType().getEnumType())
			{
				case BOOL:
					if (!Objects.equals(entity.getBoolean(attrName), otherEntity.getBoolean(attrName)))
					{
						return false;
					}
					break;
				case CATEGORICAL:
				case FILE:
				case XREF:
					Entity xrefValue = entity.getEntity(attrName);
					Entity otherXrefValue = otherEntity.getEntity(attrName);
					if (xrefValue == null && otherXrefValue != null)
					{
						return false;
					}
					if (xrefValue != null && otherXrefValue == null)
					{
						return false;
					}
					if (xrefValue != null && otherXrefValue != null && !xrefValue.getIdValue()
							.equals(otherXrefValue.getIdValue()))
					{
						return false;
					}
					break;
				case CATEGORICAL_MREF:
				case MREF:
					List<Entity> entities = newArrayList(entity.getEntities(attrName));
					List<Entity> otherEntities = newArrayList(otherEntity.getEntities(attrName));
					if (entities.size() != otherEntities.size())
					{
						return false;
					}
					for (int i = 0; i < entities.size(); ++i)
					{
						Entity mrefValue = entities.get(i);
						Entity otherMrefValue = otherEntities.get(i);
						if (mrefValue == null && otherMrefValue != null)
						{
							return false;
						}
						if (mrefValue != null && otherMrefValue == null)
						{
							return false;
						}
						if (mrefValue != null && otherMrefValue != null && !mrefValue.getIdValue()
								.equals(otherMrefValue.getIdValue()))
						{
							return false;
						}
					}
					break;
				case COMPOUND:
					throw new RuntimeException(format("Invalid data type [%s]", attr.getDataType().getEnumType()));
				case DATE:
					if (!Objects.equals(entity.getDate(attrName), otherEntity.getDate(attrName)))
					{
						return false;
					}
					break;
				case DATE_TIME:
					if (!Objects.equals(entity.getTimestamp(attrName), otherEntity.getTimestamp(attrName)))
					{
						return false;
					}
					break;
				case DECIMAL:
					if (!Objects.equals(entity.getDouble(attrName), otherEntity.getDouble(attrName)))
					{
						return false;
					}
					break;
				case EMAIL:
				case ENUM:
				case HTML:
				case HYPERLINK:
				case SCRIPT:
				case STRING:
				case TEXT:
					if (!Objects.equals(entity.getString(attrName), otherEntity.getString(attrName)))
					{
						return false;
					}
					break;
				case INT:
					if (!Objects.equals(entity.getInt(attrName), otherEntity.getInt(attrName)))
					{
						return false;
					}
					break;
				case LONG:
					if (!Objects.equals(entity.getLong(attrName), otherEntity.getLong(attrName)))
					{
						return false;
					}
					break;
				default:
					throw new RuntimeException(format("Unknown data type [%s]", attr.getDataType().getEnumType()));
			}
		}
		return true;
	}

	public static int hashCode(Entity entity)
	{
		int h = 0;
		for (AttributeMetaData attr : entity.getEntityMetaData().getAtomicAttributes())
		{
			int hValue = 0;
			String attrName = attr.getName();
			switch (attr.getDataType().getEnumType())
			{
				case BOOL:
					hValue = Objects.hashCode(entity.getBoolean(attrName));
					break;
				case CATEGORICAL:
				case FILE:
				case XREF:
					Entity xrefValue = entity.getEntity(attrName);
					Object xrefIdValue = xrefValue != null ? xrefValue.getIdValue() : null;
					hValue = Objects.hashCode(xrefIdValue);
					break;
				case CATEGORICAL_MREF:
				case MREF:
					for (Entity mrefValue : entity.getEntities(attrName))
					{
						Object mrefIdValue = mrefValue != null ? mrefValue.getIdValue() : null;
						hValue += Objects.hashCode(mrefIdValue);
					}
					break;
				case COMPOUND:
					throw new RuntimeException(format("Invalid data type [%s]", attr.getDataType().getEnumType()));
				case DATE:
					hValue = Objects.hashCode(entity.getDate(attrName));
					break;
				case DATE_TIME:
					hValue = Objects.hashCode(entity.getTimestamp(attrName));
					break;
				case DECIMAL:
					hValue = Objects.hashCode(entity.getDouble(attrName));
					break;
				case EMAIL:
				case ENUM:
				case HTML:
				case HYPERLINK:
				case SCRIPT:
				case STRING:
				case TEXT:
					hValue = Objects.hashCode(entity.getString(attrName));
					break;
				case INT:
					hValue = Objects.hashCode(entity.getInt(attrName));
					break;
				case LONG:
					hValue = Objects.hashCode(entity.getLong(attrName));
					break;
				default:
					throw new RuntimeException(format("Unknown data type [%s]", attr.getDataType().getEnumType()));
			}
			h += Objects.hashCode(attrName) ^ hValue;
		}

		int result = entity.getEntityMetaData().getName().hashCode();
		return 31 * result + h;
	}
}
