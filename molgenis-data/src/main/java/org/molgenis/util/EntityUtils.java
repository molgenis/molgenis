package org.molgenis.util;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.support.EntityTypeUtils;

import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.util.MolgenisDateFormat.*;

public class EntityUtils
{
	/**
	 * Convert a string value to a typed value based on a non-entity-referencing attribute data type.
	 *
	 * @param valueStr string value
	 * @param attr     non-entity-referencing attribute
	 * @return typed value
	 * @throws MolgenisDataException if attribute references another entity
	 */
	public static Object getTypedValue(String valueStr, Attribute attr)
	{
		//Reference types cannot be processed because we lack an entityManager in this route.
		if (EntityTypeUtils.isReferenceType(attr))
		{
			throw new MolgenisDataException(
					"getTypedValue(String, AttributeMetadata) can't be used for attributes referencing entities");
		}
		return getTypedValue(valueStr, attr, null);
	}

	/**
	 * Convert a string value to a typed value based on the attribute data type.
	 *
	 * @param valueStr      string value
	 * @param attr          attribute
	 * @param entityManager entity manager used to convert referenced entity values
	 * @return typed value
	 */
	public static Object getTypedValue(String valueStr, Attribute attr, EntityManager entityManager)
	{
		if (valueStr == null) return null;
		switch (attr.getDataType())
		{
			case BOOL:
				return Boolean.valueOf(valueStr);
			case CATEGORICAL:
			case FILE:
			case XREF:
				EntityType xrefEntity = attr.getRefEntity();
				Object xrefIdValue = getTypedValue(valueStr, xrefEntity.getIdAttribute(), entityManager);
				return entityManager.getReference(xrefEntity, xrefIdValue);
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				EntityType mrefEntity = attr.getRefEntity();
				List<String> mrefIdStrValues = ListEscapeUtils.toList(valueStr);
				return mrefIdStrValues.stream()
									  .map(mrefIdStrValue -> getTypedValue(mrefIdStrValue, mrefEntity.getIdAttribute(),
											  entityManager))
									  .map(mrefIdValue -> entityManager.getReference(mrefEntity, mrefIdValue))
									  .collect(toList());
			case COMPOUND:
				throw new IllegalArgumentException("Compound attribute has no value");
			case DATE:
				try
				{
					return parseLocalDate(valueStr);
				}
				catch (DateTimeParseException e)
				{
					throw new MolgenisDataException(
							format(FAILED_TO_PARSE_ATTRIBUTE_AS_DATE_MESSAGE, attr.getName(), valueStr), e);
				}
			case DATE_TIME:
				try
				{
					return parseInstant(valueStr);
				}
				catch (DateTimeParseException e)
				{
					throw new MolgenisDataException(
							format(FAILED_TO_PARSE_ATTRIBUTE_AS_DATETIME_MESSAGE, attr.getName(), valueStr), e);
				}
			case DECIMAL:
				return Double.valueOf(valueStr);
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				return valueStr;
			case INT:
				return Integer.valueOf(valueStr);
			case LONG:
				return Long.valueOf(valueStr);
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attr.getDataType().toString()));
		}
	}

	/**
	 * Checks if an entity contains data or not
	 *
	 * @param entity
	 */
	public static boolean isEmpty(Entity entity)
	{
		for (String attr : entity.getAttributeNames())
		{
			if (entity.get(attr) != null) return false;
		}
		return true;
	}

	public static List<Pair<EntityType, List<Attribute>>> getReferencingEntityType(EntityType entityType,
			DataService dataService)
	{
		List<Pair<EntityType, List<Attribute>>> referencingEntityType = newArrayList();

		// get entity types that referencing the given entity (including self)
		String entityTypeId = entityType.getId();
		dataService.getEntityTypeIds().forEach(otherEntityName ->
		{
			EntityType otherEntityType = dataService.getEntityType(otherEntityName);

			// get referencing attributes for other entity
			List<Attribute> referencingAttributes = null;
			for (Attribute attribute : otherEntityType.getAtomicAttributes())
			{
				EntityType refEntityType = attribute.getRefEntity();
				if (refEntityType != null && refEntityType.getId().equals(entityTypeId))
				{
					if (referencingAttributes == null) referencingAttributes = newArrayList();
					referencingAttributes.add(attribute);
				}
			}

			// store references
			if (referencingAttributes != null)
			{
				referencingEntityType.add(new Pair<>(otherEntityType, referencingAttributes));
			}
		});

		return referencingEntityType;
	}

	/**
	 * Gets all attribute names of an EntityType (atomic + compound)
	 *
	 * @param entityType
	 * @return
	 */
	public static Iterable<String> getAttributeNames(EntityType entityType)
	{
		// atomic
		Iterable<String> atomicAttributes = transform(entityType.getAtomicAttributes(), Attribute::getName);

		// compound
		Iterable<String> compoundAttributes = transform(
				filter(entityType.getAttributes(), attribute -> attribute.getDataType() == COMPOUND),
				Attribute::getName);

		// all = atomic + compound
		return concat(atomicAttributes, compoundAttributes);
	}

	/**
	 * Checks if an entity has another entity as one of its parents
	 *
	 * @param entityType
	 * @param entityTypeId
	 * @return
	 */
	public static boolean doesExtend(EntityType entityType, String entityTypeId)
	{
		EntityType parent = entityType.getExtends();
		while (parent != null)
		{
			if (parent.getId().equalsIgnoreCase(entityTypeId)) return true;
			parent = parent.getExtends();
		}
		return false;
	}

	/**
	 * Get an Iterable of entities as a stream of entities
	 *
	 * @param entities entities iterable
	 * @return entities stream
	 */
	public static <E extends Entity> Stream<E> asStream(Iterable<E> entities)
	{
		return stream(entities.spliterator(), false);
	}

	/**
	 * Returns true if entity metadata equals another entity metadata. TODO docs
	 *
	 * @param entityType
	 * @param otherEntityType
	 * @return
	 */
	public static boolean equals(EntityType entityType, EntityType otherEntityType)
	{
		if (entityType == null && otherEntityType != null) return false;
		if (entityType != null && otherEntityType == null) return false;
		if (!entityType.getId().equals(otherEntityType.getId())) return false;
		if (!Objects.equals(entityType.getLabel(), otherEntityType.getLabel())) return false;
		if (!Objects.equals(entityType.getDescription(), otherEntityType.getDescription())) return false;
		if (entityType.isAbstract() != otherEntityType.isAbstract()) return false;

		//NB Thsi is at such a low level that we do not know the default backend
		// so we don't check if the other one is the default if the backend is null.
		String backend = entityType.getBackend();
		String otherBackend = otherEntityType.getBackend();
		if (backend == null && otherBackend != null) return false;
		else if (backend != null && otherBackend == null) return false;
		else if (backend != null && !backend.equals(otherBackend)) return false;

		// compare package identifiers
		Package package_ = entityType.getPackage();
		Package otherPackage = otherEntityType.getPackage();
		if (package_ == null && otherPackage != null) return false;
		if (package_ != null && otherPackage == null) return false;
		if (package_ != null && !package_.getIdValue().equals(otherPackage.getIdValue()))
		{
			return false;
		}

		// compare id attribute identifier (identifier might be null if id attribute hasn't been persisted yet)
		Attribute ownIdAttribute = entityType.getOwnIdAttribute();
		Attribute otherOwnIdAttribute = otherEntityType.getOwnIdAttribute();
		if (ownIdAttribute == null && otherOwnIdAttribute != null) return false;
		if (ownIdAttribute != null && otherOwnIdAttribute == null) return false;
		if (ownIdAttribute != null && otherOwnIdAttribute != null && !Objects.equals(ownIdAttribute.getIdentifier(),
				otherOwnIdAttribute.getIdentifier())) return false;

		// compare label attribute identifier (identifier might be null if id attribute hasn't been persisted yet)
		Attribute ownLabelAttribute = entityType.getOwnLabelAttribute();
		Attribute otherOwnLabelAttribute = otherEntityType.getOwnLabelAttribute();
		if (ownLabelAttribute == null && otherOwnLabelAttribute != null) return false;
		if (ownLabelAttribute != null && otherOwnLabelAttribute == null) return false;
		if (ownLabelAttribute != null && otherOwnLabelAttribute != null && !Objects.equals(
				ownLabelAttribute.getIdentifier(), otherOwnLabelAttribute.getIdentifier())) return false;

		// compare lookup attribute identifiers
		List<Attribute> lookupAttrs = newArrayList(entityType.getOwnLookupAttributes());
		List<Attribute> otherLookupAttrs = newArrayList(otherEntityType.getOwnLookupAttributes());
		if (lookupAttrs.size() != otherLookupAttrs.size()) return false;
		for (int i = 0; i < lookupAttrs.size(); ++i)
		{
			// identifier might be null if id attribute hasn't been persisted yet
			if (!Objects.equals(lookupAttrs.get(i).getIdentifier(), otherLookupAttrs.get(i).getIdentifier()))
			{
				return false;
			}
		}

		// compare extends entity identifier
		EntityType extendsEntityType = entityType.getExtends();
		EntityType otherExtendsEntityType = otherEntityType.getExtends();
		if (extendsEntityType == null && otherExtendsEntityType != null) return false;
		if (extendsEntityType != null && otherExtendsEntityType == null) return false;
		if (extendsEntityType != null && otherExtendsEntityType != null && !extendsEntityType.getId()
																							 .equals(otherExtendsEntityType
																									 .getId()))
			return false;

		// compare attributes
		if (!equals(entityType.getOwnAllAttributes(), otherEntityType.getOwnAllAttributes())) return false;

		// compare tag identifiers
		List<Tag> tags = newArrayList(entityType.getTags());
		List<Tag> otherTags = newArrayList(otherEntityType.getTags());
		if (tags.size() != otherTags.size()) return false;
		for (int i = 0; i < tags.size(); ++i)
		{
			if (!tags.get(i).getId().equals(otherTags.get(i).getId())) return false;
		}

		return entityType.getIndexingDepth() == otherEntityType.getIndexingDepth();
	}

	/**
	 * Returns true if an Iterable equals another Iterable.
	 *
	 * @param attrsIt
	 * @param otherAttrsIt
	 * @return
	 */
	public static boolean equals(Iterable<Attribute> attrsIt, Iterable<Attribute> otherAttrsIt)
	{
		List<Attribute> attrs = newArrayList(attrsIt);
		List<Attribute> otherAttrs = newArrayList(otherAttrsIt);

		if (attrs.size() != otherAttrs.size()) return false;
		for (int i = 0; i < attrs.size(); ++i)
		{
			if (!equals(attrs.get(i), otherAttrs.get(i))) return false;
		}
		return true;
	}

	/**
	 * Returns true if an Iterable equals another Iterable.
	 *
	 * @param entityIterable
	 * @param otherEntityIterable
	 * @return
	 */
	public static boolean equalsEntities(Iterable<Entity> entityIterable, Iterable<Entity> otherEntityIterable)
	{
		List<Entity> attrs = newArrayList(entityIterable);
		List<Entity> otherAttrs = newArrayList(otherEntityIterable);

		if (attrs.size() != otherAttrs.size()) return false;
		for (int i = 0; i < attrs.size(); ++i)
		{
			if (!equals(attrs.get(i), otherAttrs.get(i))) return false;
		}
		return true;
	}

	/**
	 * Returns true if a Tag equals another Tag.
	 *
	 * @param tag
	 * @param otherTag
	 * @return
	 */
	public static boolean equals(Tag tag, Tag otherTag)
	{
		if (!Objects.equals(tag.getId(), otherTag.getId())) return false;
		if (!Objects.equals(tag.getObjectIri(), otherTag.getObjectIri())) return false;
		if (!Objects.equals(tag.getLabel(), otherTag.getLabel())) return false;
		if (!Objects.equals(tag.getRelationIri(), otherTag.getRelationIri())) return false;
		if (!Objects.equals(tag.getRelationLabel(), otherTag.getRelationLabel())) return false;
		if (!Objects.equals(tag.getCodeSystem(), otherTag.getCodeSystem())) return false;

		return true;
	}

	/**
	 * Returns true if an attribute equals another attribute.
	 *
	 * @param attr
	 * @param otherAttr
	 * @return
	 */
	public static boolean equals(Attribute attr, Attribute otherAttr)
	{
		return equals(attr, otherAttr, true);
	}

	/**
	 * Returns true if an attribute equals another attribute.
	 * Skips the identifier if checkIdentifier is set to false
	 * <p>
	 * Other attribute identifiers can be null when importing and this attribute
	 * has not been persisted to the db yet
	 * </p>
	 *
	 * @param attr
	 * @param otherAttr
	 * @param checkIdentifier skips checking attribute identifier, parent attribute identifier and attribute entity identifier
	 * @return
	 */
	public static boolean equals(Attribute attr, Attribute otherAttr, boolean checkIdentifier)
	{
		if (attr == null || otherAttr == null)
		{
			if (attr == null && otherAttr == null) return true;
			return false;
		}

		if (checkIdentifier) if (!Objects.equals(attr.getIdentifier(), otherAttr.getIdentifier())) return false;
		if (!Objects.equals(attr.getName(), otherAttr.getName())) return false;

		EntityType entity = attr.getEntity();
		EntityType otherEntity = otherAttr.getEntity();
		if (checkIdentifier)
		{
			if (entity == null && otherEntity != null) return false;
			if (entity != null && otherEntity == null) return false;
			if (entity != null && !entity.getId().equals(otherEntity.getId())) return false;
		}
		if (!Objects.equals(attr.getSequenceNumber(), otherAttr.getSequenceNumber())) return false;
		if (!Objects.equals(attr.getLabel(), otherAttr.getLabel())) return false;
		if (!Objects.equals(attr.getDescription(), otherAttr.getDescription())) return false;
		if (!Objects.equals(attr.getDataType(), otherAttr.getDataType())) return false;
		if (!Objects.equals(attr.isIdAttribute(), otherAttr.isIdAttribute())) return false;
		if (!Objects.equals(attr.isLabelAttribute(), otherAttr.isLabelAttribute())) return false;
		if (!Objects.equals(attr.getLookupAttributeIndex(), otherAttr.getLookupAttributeIndex())) return false;

		// recursively compare attribute parent
		if (!EntityUtils.equals(attr.getParent(), otherAttr.getParent(), checkIdentifier)) return false;

		// compare entity identifier
		EntityType refEntity = attr.getRefEntity();
		EntityType otherRefEntity = otherAttr.getRefEntity();
		if (refEntity == null && otherRefEntity != null) return false;
		if (refEntity != null && otherRefEntity == null) return false;
		if (refEntity != null && otherRefEntity != null && !refEntity.getId().equals(otherRefEntity.getId()))
			return false;
		if (!EntityUtils.equals(attr.getMappedBy(), otherAttr.getMappedBy())) return false;
		if (!Objects.equals(attr.getOrderBy(), otherAttr.getOrderBy())) return false;
		if (!Objects.equals(attr.getExpression(), otherAttr.getExpression())) return false;
		if (!Objects.equals(attr.isNillable(), otherAttr.isNillable())) return false;
		if (!Objects.equals(attr.isAuto(), otherAttr.isAuto())) return false;
		if (!Objects.equals(attr.isVisible(), otherAttr.isVisible())) return false;
		if (!Objects.equals(attr.isAggregatable(), otherAttr.isAggregatable())) return false;
		if (!Objects.equals(attr.getEnumOptions(), otherAttr.getEnumOptions())) return false;
		if (!Objects.equals(attr.getRangeMin(), otherAttr.getRangeMin())) return false;
		if (!Objects.equals(attr.getRangeMax(), otherAttr.getRangeMax())) return false;
		if (!Objects.equals(attr.isReadOnly(), otherAttr.isReadOnly())) return false;
		if (!Objects.equals(attr.isUnique(), otherAttr.isUnique())) return false;
		if (!Objects.equals(attr.getVisibleExpression(), otherAttr.getVisibleExpression())) return false;
		if (!Objects.equals(attr.getValidationExpression(), otherAttr.getValidationExpression())) return false;
		if (!Objects.equals(attr.getDefaultValue(), otherAttr.getDefaultValue())) return false;

		// compare tag identifiers
		List<Tag> tags = newArrayList(attr.getTags());
		List<Tag> otherTags = newArrayList(otherAttr.getTags());
		if (tags.size() != otherTags.size()) return false;
		for (int i = 0; i < tags.size(); ++i)
		{
			if (!Objects.equals(tags.get(i).getId(), otherTags.get(i).getId())) return false;
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
		if (entity == null && otherEntity != null) return false;
		if (entity != null && otherEntity == null) return false;
		if (!entity.getEntityType().getId().equals(otherEntity.getEntityType().getId())) return false;
		for (Attribute attr : entity.getEntityType().getAtomicAttributes())
		{
			String attrName = attr.getName();
			switch (attr.getDataType())
			{
				case BOOL:
					if (!Objects.equals(entity.getBoolean(attrName), otherEntity.getBoolean(attrName))) return false;
					break;
				case CATEGORICAL:
				case FILE:
				case XREF:
					Entity xrefValue = entity.getEntity(attrName);
					Entity otherXrefValue = otherEntity.getEntity(attrName);
					if (xrefValue == null && otherXrefValue != null) return false;
					if (xrefValue != null && otherXrefValue == null) return false;
					if (xrefValue != null && otherXrefValue != null && !xrefValue.getIdValue()
																				 .equals(otherXrefValue.getIdValue()))
						return false;
					break;
				case CATEGORICAL_MREF:
				case ONE_TO_MANY:
				case MREF:
					List<Entity> entities = newArrayList(entity.getEntities(attrName));
					List<Entity> otherEntities = newArrayList(otherEntity.getEntities(attrName));
					if (entities.size() != otherEntities.size()) return false;
					for (int i = 0; i < entities.size(); ++i)
					{
						Entity mrefValue = entities.get(i);
						Entity otherMrefValue = otherEntities.get(i);
						if (mrefValue == null && otherMrefValue != null) return false;
						if (mrefValue != null && otherMrefValue == null) return false;
						if (mrefValue != null && otherMrefValue != null && !mrefValue.getIdValue()
																					 .equals(otherMrefValue.getIdValue()))
							return false;
					}
					break;
				case COMPOUND:
					throw new RuntimeException(format("Invalid data type [%s]", attr.getDataType()));
				case DATE:
					if (!Objects.equals(entity.getLocalDate(attrName), otherEntity.getLocalDate(attrName)))
						return false;
					break;
				case DATE_TIME:
					if (!Objects.equals(entity.getInstant(attrName), otherEntity.getInstant(attrName))) return false;
					break;
				case DECIMAL:
					if (!Objects.equals(entity.getDouble(attrName), otherEntity.getDouble(attrName))) return false;
					break;
				case EMAIL:
				case ENUM:
				case HTML:
				case HYPERLINK:
				case SCRIPT:
				case STRING:
				case TEXT:
					if (!Objects.equals(entity.getString(attrName), otherEntity.getString(attrName))) return false;
					break;
				case INT:
					if (!Objects.equals(entity.getInt(attrName), otherEntity.getInt(attrName))) return false;
					break;
				case LONG:
					if (!Objects.equals(entity.getLong(attrName), otherEntity.getLong(attrName))) return false;
					break;
				default:
					throw new RuntimeException(format("Unknown data type [%s]", attr.getDataType()));
			}
		}
		return true;
	}

	public static int hashCode(Entity entity)
	{
		int h = 0;
		for (Attribute attr : entity.getEntityType().getAtomicAttributes())
		{
			int hValue = 0;
			String attrName = attr.getName();
			switch (attr.getDataType())
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
				case ONE_TO_MANY:
				case MREF:
					for (Entity mrefValue : entity.getEntities(attrName))
					{
						Object mrefIdValue = mrefValue != null ? mrefValue.getIdValue() : null;
						hValue += Objects.hashCode(mrefIdValue);
					}
					break;
				case COMPOUND:
					throw new RuntimeException(format("Invalid data type [%s]", attr.getDataType()));
				case DATE:
					hValue = Objects.hashCode(entity.getLocalDate(attrName));
					break;
				case DATE_TIME:
					hValue = Objects.hashCode(entity.getInstant(attrName));
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
					throw new RuntimeException(format("Unknown data type [%s]", attr.getDataType()));
			}
			h += Objects.hashCode(attrName) ^ hValue;
		}

		int result = entity.getEntityType().getId().hashCode();
		return 31 * result + h;
	}

	public static boolean entitiesEquals(Iterable<Entity> entities, Iterable<Entity> other)
	{
		if (size(entities) != size(other)) return false;
		Iterator<Entity> otherIt = other.iterator();
		for (Entity entity : entities)
		{
			if (!equals(entity, otherIt.next())) return false;
		}
		return true;
	}
}
