package org.molgenis.data.validation;

import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.HugeMap;
import org.molgenis.util.HugeSet;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.data.support.EntityTypeUtils.*;

public class RepositoryValidationDecorator extends AbstractRepositoryDecorator<Entity>
{
	private enum ValidationMode
	{
		ADD, UPDATE
	}

	private final DataService dataService;
	private final Repository<Entity> decoratedRepository;
	private final EntityAttributesValidator entityAttributesValidator;
	private final ExpressionValidator expressionValidator;

	public RepositoryValidationDecorator(DataService dataService, Repository<Entity> repository,
			EntityAttributesValidator entityAttributesValidator, ExpressionValidator expressionValidator)
	{
		this.dataService = requireNonNull(dataService);
		this.decoratedRepository = requireNonNull(repository);
		this.entityAttributesValidator = requireNonNull(entityAttributesValidator);
		this.expressionValidator = requireNonNull(expressionValidator);
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepository;
	}

	@Override
	public void update(Entity entity)
	{
		try (ValidationResource validationResource = new ValidationResource())
		{
			entity = validate(Stream.of(entity), validationResource, ValidationMode.UPDATE).findFirst().get();
		}
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		try (ValidationResource validationResource = new ValidationResource())
		{
			entities = validate(entities, validationResource, ValidationMode.UPDATE);
			decoratedRepository.update(entities);
		}
	}

	@Override
	public void add(Entity entity)
	{
		try (ValidationResource validationResource = new ValidationResource())
		{
			entity = validate(Stream.of(entity), validationResource, ValidationMode.ADD).findFirst().get();
		}
		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		try (ValidationResource validationResource = new ValidationResource())
		{
			entities = validate(entities, validationResource, ValidationMode.ADD);
			return decoratedRepository.add(entities);
		}
	}

	private Stream<Entity> validate(Stream<Entity> entities, ValidationResource validationResource,
			ValidationMode validationMode)
	{
		// prepare validation
		initValidation(validationResource, validationMode);

		boolean validateRequired = !getCapabilities().contains(VALIDATE_NOTNULL_CONSTRAINT);
		boolean validateUniqueness = !getCapabilities().contains(VALIDATE_UNIQUE_CONSTRAINT);
		boolean validateReadonly = !getCapabilities().contains(VALIDATE_READONLY_CONSTRAINT);

		// add validation operation to stream
		return entities.filter(entity ->
		{
			validationResource.incrementRow();

			validateEntityValueTypes(entity, validationResource);

			// other validation steps might not be able to handle invalid data types, stop here
			if (validationResource.hasViolations())
			{
				throw new MolgenisValidationException(validationResource.getViolations());
			}

			if (validateRequired)
			{
				validateEntityValueRequired(entity, validationResource);
			}

			if (validateUniqueness)
			{
				validateEntityValueUniqueness(entity, validationResource, validationMode);
			}

			validateEntityValueReferences(entity, validationResource);

			if (validateReadonly && validationMode == ValidationMode.UPDATE)
			{
				validateEntityValueReadOnly(entity, validationResource);
			}

			if (validationResource.hasViolations())
			{
				throw new MolgenisValidationException(validationResource.getViolations());
			}

			return true;
		});
	}

	private void initValidation(ValidationResource validationResource, ValidationMode validationMode)
	{
		initRequiredValueValidation(validationResource);
		initReferenceValidation(validationResource);
		initUniqueValidation(validationResource);
		if (validationMode == ValidationMode.UPDATE)
		{
			initReadonlyValidation(validationResource);
		}
	}

	private void initRequiredValueValidation(ValidationResource validationResource)
	{
		if (!getCapabilities().contains(VALIDATE_NOTNULL_CONSTRAINT))
		{
			List<Attribute> requiredValueAttrs = stream(getEntityType().getAtomicAttributes().spliterator(),
					false).filter(attr -> !attr.isNillable() && attr.getExpression() == null).collect(toList());

			validationResource.setRequiredValueAttrs(requiredValueAttrs);
		}
	}

	private void initReferenceValidation(ValidationResource validationResource)
	{
		// get reference attrs
		List<Attribute> refAttrs;
		if (!getCapabilities().contains(VALIDATE_REFERENCE_CONSTRAINT))
		{
			// get reference attrs
			refAttrs = stream(getEntityType().getAtomicAttributes().spliterator(), false).filter(
					attr -> isReferenceType(attr) && attr.getExpression() == null).collect(toList());
		}
		else
		{
			// validate cross-repository collection reference constraints. the decorated repository takes care of
			// validating other reference constraints
			String backend = dataService.getMeta().getBackend(getEntityType()).getName();
			refAttrs = stream(getEntityType().getAtomicAttributes().spliterator(), false).filter(
					attr -> isReferenceType(attr) && attr.getExpression() == null && isDifferentBackend(backend, attr))
																						 .collect(toList());
		}

		// get referenced entity ids
		if (!refAttrs.isEmpty())
		{
			Map<String, HugeSet<Object>> refEntitiesIds = new HashMap<>();
			refAttrs.forEach(refAttr ->
			{
				EntityType refEntityType = refAttr.getRefEntity();
				String refEntityName = refEntityType.getId();
				HugeSet<Object> refEntityIds = refEntitiesIds.get(refEntityName);
				if (refEntityIds == null)
				{
					refEntityIds = new HugeSet<>();
					refEntitiesIds.put(refEntityName, refEntityIds);

					Query<Entity> q = new QueryImpl<>().fetch(
							new Fetch().field(refEntityType.getIdAttribute().getName()));
					for (Iterator<Entity> it = dataService.findAll(refEntityName, q).iterator(); it.hasNext(); )
					{
						refEntityIds.add(it.next().getIdValue());
					}
				}
			});

			validationResource.setRefEntitiesIds(refEntitiesIds);
		}

		validationResource.setSelfReferencing(
				refAttrs.stream().anyMatch(refAttr -> refAttr.getRefEntity().getId().equals(getEntityType().getId())));
		validationResource.setRefAttrs(refAttrs);
	}

	private boolean isDifferentBackend(String backend, Attribute attr)
	{
		EntityType refEntity = attr.getRefEntity();
		String refEntityBackend = dataService.getMeta().getBackend(refEntity).getName();
		return !backend.equals(refEntityBackend);
	}

	private void initUniqueValidation(ValidationResource validationResource)
	{
		if (!getCapabilities().contains(VALIDATE_UNIQUE_CONSTRAINT))
		{
			// get unique attributes
			List<Attribute> uniqueAttrs = stream(getEntityType().getAtomicAttributes().spliterator(), false).filter(
					attr -> attr.isUnique() && attr.getExpression() == null).collect(toList());

			// get existing values for each attributes
			if (!uniqueAttrs.isEmpty())
			{
				Map<String, HugeMap<Object, Object>> uniqueAttrsValues = new HashMap<>();

				Fetch fetch = new Fetch();
				uniqueAttrs.forEach(uniqueAttr ->
				{
					uniqueAttrsValues.put(uniqueAttr.getName(), new HugeMap<>());
					fetch.field(uniqueAttr.getName());
				});

				Query<Entity> q = new QueryImpl<>().fetch(fetch);
				decoratedRepository.findAll(q).forEach(entity -> uniqueAttrs.forEach(uniqueAttr ->
				{
					HugeMap<Object, Object> uniqueAttrValues = uniqueAttrsValues.get(uniqueAttr.getName());
					Object attrValue = entity.get(uniqueAttr.getName());
					if (attrValue != null)
					{
						if (isSingleReferenceType(uniqueAttr))
						{
							attrValue = ((Entity) attrValue).getIdValue();
						}
						uniqueAttrValues.put(attrValue, entity.getIdValue());
					}
				}));

				validationResource.setUniqueAttrsValues(uniqueAttrsValues);
			}

			validationResource.setUniqueAttrs(uniqueAttrs);
		}
	}

	private void initReadonlyValidation(ValidationResource validationResource)
	{
		if (!getCapabilities().contains(VALIDATE_READONLY_CONSTRAINT))
		{
			String idAttrName = getEntityType().getIdAttribute().getName();
			List<Attribute> readonlyAttrs = stream(getEntityType().getAtomicAttributes().spliterator(), false).filter(
					attr -> attr.isReadOnly() && attr.getExpression() == null && !attr.isMappedBy() && !attr.getName()
																											.equals(idAttrName))
																											  .collect(
																													  toList());

			validationResource.setReadonlyAttrs(readonlyAttrs);
		}
	}

	private void validateEntityValueRequired(Entity entity, ValidationResource validationResource)
	{
		validationResource.getRequiredValueAttrs().forEach(nonNillableAttr ->
		{
			Object value = entity.get(nonNillableAttr.getName());
			if (value == null || (isMultipleReferenceType(nonNillableAttr) && !entity.getEntities(
					nonNillableAttr.getName()).iterator().hasNext()))
			{
				ConstraintViolation constraintViolation = new ConstraintViolation(
						format("The attribute '%s' of entity '%s' can not be null.", nonNillableAttr.getName(),
								getName()), nonNillableAttr, Integer.valueOf(validationResource.getRow()).longValue());
				validationResource.addViolation(constraintViolation);
			}
		});
	}

	private void validateEntityValueTypes(Entity entity, ValidationResource validationResource)
	{
		// entity attributes validation
		Set<ConstraintViolation> attrViolations = entityAttributesValidator.validate(entity, getEntityType());
		if (attrViolations != null && !attrViolations.isEmpty())
		{
			attrViolations.forEach(validationResource::addViolation);
		}
	}

	private void validateEntityValueUniqueness(Entity entity, ValidationResource validationResource,
			ValidationMode validationMode)
	{
		validationResource.getUniqueAttrs().forEach(uniqueAttr ->
		{
			Object attrValue = entity.get(uniqueAttr.getName());
			if (attrValue != null)
			{
				if (isSingleReferenceType(uniqueAttr))
				{
					attrValue = ((Entity) attrValue).getIdValue();
				}

				HugeMap<Object, Object> uniqueAttrValues = validationResource.getUniqueAttrsValues()
																			 .get(uniqueAttr.getName());
				Object existingEntityId = uniqueAttrValues.get(attrValue);
				if ((validationMode == ValidationMode.ADD && existingEntityId != null) || (
						validationMode == ValidationMode.UPDATE && existingEntityId != null && !existingEntityId.equals(
								entity.getIdValue())))
				{
					ConstraintViolation constraintViolation = new ConstraintViolation(
							format("Duplicate value '%s' for unique attribute '%s' from entity '%s'", attrValue,
									uniqueAttr.getName(), getName()), uniqueAttr, (long) validationResource.getRow());
					validationResource.addViolation(constraintViolation);
				}
				else
				{
					uniqueAttrValues.put(attrValue, entity.getIdValue());
				}
			}
		});
	}

	private void validateEntityValueReferences(Entity entity, ValidationResource validationResource)
	{
		validationResource.getRefAttrs().forEach(refAttr ->
		{
			HugeSet<Object> refEntityIds = validationResource.getRefEntitiesIds().get(refAttr.getRefEntity().getId());

			Iterable<Entity> refEntities;
			if (isSingleReferenceType(refAttr))
			{
				Entity refEntity = entity.getEntity(refAttr.getName());
				if (refEntity != null)
				{
					refEntities = singleton(refEntity);
				}
				else
				{
					refEntities = emptyList();
				}
			}
			else
			{
				refEntities = entity.getEntities(refAttr.getName());
			}

			for (Entity refEntity : refEntities)
			{
				if (!refEntityIds.contains(refEntity.getIdValue()))
				{
					boolean selfReference = entity.getEntityType().getId().equals(refAttr.getRefEntity().getId());
					if (!(selfReference && entity.getIdValue().equals(refEntity.getIdValue())))
					{
						String message = String.format("Unknown xref value '%s' for attribute '%s' of entity '%s'.",
								DataConverter.toString(refEntity.getIdValue()), refAttr.getName(), getName());

						ConstraintViolation constraintViolation = new ConstraintViolation(message, refAttr,
								(long) validationResource.getRow());
						validationResource.addViolation(constraintViolation);
					}
				}
			}

			// only do if self reference
			if (validationResource.isSelfReferencing())
			{
				validationResource.addRefEntityId(getName(), entity.getIdValue());
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void validateEntityValueReadOnly(Entity entity, ValidationResource validationResource)
	{
		if (validationResource.getReadonlyAttrs().isEmpty())
		{
			return;
		}

		Entity entityToUpdate = findOneById(entity.getIdValue());
		validationResource.getReadonlyAttrs().forEach(readonlyAttr ->
		{
			Object value = entity.get(readonlyAttr.getName());

			Object existingValue = entityToUpdate.get(readonlyAttr.getName());

			if (isSingleReferenceType(readonlyAttr))
			{
				if (value != null)
				{
					value = ((Entity) value).getIdValue();
				}
				if (existingValue != null)
				{
					existingValue = ((Entity) existingValue).getIdValue();
				}
			}
			else if (isMultipleReferenceType(readonlyAttr))
			{
				value = stream(entity.getEntities(readonlyAttr.getName()).spliterator(), false).map(Entity::getIdValue)
																							   .collect(toList());

				existingValue = stream(entityToUpdate.getEntities(readonlyAttr.getName()).spliterator(), false).map(
						Entity::getIdValue).collect(toList());
			}

			if (value != null && existingValue != null && !value.equals(existingValue))
			{
				validationResource.addViolation(new ConstraintViolation(
						format("The attribute '%s' of entity '%s' can not be changed it is readonly.",
								readonlyAttr.getName(), getName()), (long) validationResource.getRow()));
			}
		});
	}

	/**
	 * Container with validation data used during stream validation
	 */
	private static class ValidationResource implements AutoCloseable
	{
		private AtomicInteger rowNr = new AtomicInteger();
		private List<Attribute> requiredValueAttrs;
		private List<Attribute> refAttrs;
		private Map<String, HugeSet<Object>> refEntitiesIds;
		private List<Attribute> uniqueAttrs;
		private Map<String, HugeMap<Object, Object>> uniqueAttrsValues;
		private List<Attribute> readonlyAttrs;
		private boolean selfReferencing;
		private Set<ConstraintViolation> violations;

		public ValidationResource()
		{
			rowNr = new AtomicInteger();
		}

		public int getRow()
		{
			return rowNr.get();
		}

		public void incrementRow()
		{
			rowNr.incrementAndGet();
		}

		public List<Attribute> getRequiredValueAttrs()
		{
			return requiredValueAttrs != null ? unmodifiableList(requiredValueAttrs) : emptyList();
		}

		public void setRequiredValueAttrs(List<Attribute> requiredValueAttrs)
		{
			this.requiredValueAttrs = requiredValueAttrs;
		}

		public List<Attribute> getRefAttrs()
		{
			return unmodifiableList(refAttrs);
		}

		public void setRefAttrs(List<Attribute> refAttrs)
		{
			this.refAttrs = refAttrs;
		}

		public Map<String, HugeSet<Object>> getRefEntitiesIds()
		{
			return refEntitiesIds != null ? unmodifiableMap(refEntitiesIds) : emptyMap();
		}

		public void setRefEntitiesIds(Map<String, HugeSet<Object>> refEntitiesIds)
		{
			this.refEntitiesIds = refEntitiesIds;
		}

		public void addRefEntityId(String name, Object idValue)
		{
			HugeSet<Object> refEntityIds = refEntitiesIds.get(name);
			// only add entity id if this validation run requires entity
			if (refEntityIds != null)
			{
				refEntityIds.add(idValue);
			}
		}

		public List<Attribute> getUniqueAttrs()
		{
			return uniqueAttrs != null ? unmodifiableList(uniqueAttrs) : emptyList();
		}

		public void setUniqueAttrs(List<Attribute> uniqueAttrs)
		{
			this.uniqueAttrs = uniqueAttrs;
		}

		public Map<String, HugeMap<Object, Object>> getUniqueAttrsValues()
		{
			return uniqueAttrsValues != null ? unmodifiableMap(uniqueAttrsValues) : emptyMap();
		}

		public void setUniqueAttrsValues(Map<String, HugeMap<Object, Object>> uniqueAttrsValues)
		{
			this.uniqueAttrsValues = uniqueAttrsValues;
		}

		public List<Attribute> getReadonlyAttrs()
		{
			return readonlyAttrs != null ? unmodifiableList(readonlyAttrs) : emptyList();
		}

		public void setReadonlyAttrs(List<Attribute> readonlyAttrs)
		{
			this.readonlyAttrs = readonlyAttrs;
		}

		public void setSelfReferencing(boolean selfReferencing)
		{
			this.selfReferencing = selfReferencing;
		}

		public boolean isSelfReferencing()
		{
			return selfReferencing;
		}

		public boolean hasViolations()
		{
			return violations != null && !violations.isEmpty();
		}

		public void addViolation(ConstraintViolation constraintViolation)
		{
			if (violations == null)
			{
				violations = new LinkedHashSet<>();
			}
			violations.add(constraintViolation);
		}

		public Set<ConstraintViolation> getViolations()
		{
			return violations != null ? unmodifiableSet(violations) : emptySet();
		}

		@Override
		public void close()
		{
			if (refEntitiesIds != null)
			{
				for (HugeSet<Object> refEntityIds : refEntitiesIds.values())
				{
					try
					{
						refEntityIds.close();
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
			if (uniqueAttrsValues != null)
			{
				for (HugeMap<Object, Object> uniqueAttrValues : uniqueAttrsValues.values())
				{
					try
					{
						uniqueAttrValues.close();
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
		}
	}
}
