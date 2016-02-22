package org.molgenis.data.validation;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.MolgenisTransactionLogEntryMetaData;
import org.molgenis.data.transaction.MolgenisTransactionLogMetaData;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.HugeMap;
import org.molgenis.util.HugeSet;

public class RepositoryValidationDecorator implements Repository
{
	private static List<String> ENTITIES_THAT_DO_NOT_NEED_VALIDATION = Arrays
			.asList(MolgenisTransactionLogMetaData.ENTITY_NAME, MolgenisTransactionLogEntryMetaData.ENTITY_NAME);

	private enum ValidationMode
	{
		ADD, UPDATE
	}

	private final DataService dataService;
	private final Repository decoratedRepository;
	private final EntityAttributesValidator entityAttributesValidator;
	private final ExpressionValidator expressionValidator;

	public RepositoryValidationDecorator(DataService dataService, Repository repository,
			EntityAttributesValidator entityAttributesValidator, ExpressionValidator expressionValidator)
	{
		this.dataService = requireNonNull(dataService);
		this.decoratedRepository = requireNonNull(repository);
		this.entityAttributesValidator = requireNonNull(entityAttributesValidator);
		this.expressionValidator = requireNonNull(expressionValidator);
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
	public void update(Stream<? extends Entity> entities)
	{
		try (ValidationResource validationResource = new ValidationResource())
		{
			entities = validate(entities, validationResource, ValidationMode.UPDATE);
			decoratedRepository.update(entities);
		}
	}

	@Override
	public void delete(Entity entity)
	{
		decoratedRepository.delete(entity);
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
	public Integer add(Stream<? extends Entity> entities)
	{
		try (ValidationResource validationResource = new ValidationResource())
		{
			entities = validate(entities, validationResource, ValidationMode.ADD);
			return decoratedRepository.add(entities);
		}
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepository.getEntityMetaData();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public void flush()
	{
		decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepository.clearCache();
	}

	@Override
	public long count()
	{
		return decoratedRepository.count();
	}

	@Override
	public Query query()
	{
		return decoratedRepository.query();
	}

	@Override
	public long count(Query q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		return decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return decoratedRepository.findOne(id);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return decoratedRepository.findOne(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepository.findAll(ids, fetch);
	}

	@Override
	public void delete(Stream<? extends Entity> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteById(Stream<Object> ids)
	{
		decoratedRepository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		decoratedRepository.deleteAll();
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepository.getCapabilities();
	}

	@Override
	public void create()
	{
		decoratedRepository.create();

	}

	@Override
	public void drop()
	{
		decoratedRepository.drop();
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepository.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepository.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepository.removeEntityListener(entityListener);
	}

	private Stream<? extends Entity> validate(Stream<? extends Entity> entities, ValidationResource validationResource,
			ValidationMode validationMode)
	{
		if (ENTITIES_THAT_DO_NOT_NEED_VALIDATION.contains(getName()))
		{
			return entities;
		}

		// prepare validation
		initValidation(validationResource, validationMode);

		// add validation operation to stream
		return entities.filter(entity -> {
			validationResource.incrementRow();

			validateEntityValueTypes(entity, validationResource);

			// other validation steps might not be able to handle invalid data types, stop here
			if (validationResource.hasViolations())
			{
				throw new MolgenisValidationException(validationResource.getViolations());
			}

			validateEntityValueRequired(entity, validationResource);

			validateEntityValueUniqueness(entity, validationResource, validationMode);

			validateEntityValueReferences(entity, validationResource);

			if (validationMode == ValidationMode.UPDATE)
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
		List<AttributeMetaData> requiredValueAttrs = StreamSupport
				.stream(getEntityMetaData().getAtomicAttributes().spliterator(), false)
				.filter(attr -> !attr.isNillable() && attr.getExpression() == null).collect(Collectors.toList());

		validationResource.setRequiredValueAttrs(requiredValueAttrs);
	}

	private void initReferenceValidation(ValidationResource validationResource)
	{
		// get reference attrs
		List<AttributeMetaData> refAttrs = StreamSupport
				.stream(getEntityMetaData().getAtomicAttributes().spliterator(), false)
				.filter(attr -> (attr.getDataType() instanceof XrefField || attr.getDataType() instanceof MrefField)
						&& attr.getExpression() == null)
				.collect(Collectors.toList());

		// get referenced entity ids
		if (!refAttrs.isEmpty())
		{
			Map<String, HugeSet<Object>> refEntitiesIds = new HashMap<>();
			refAttrs.forEach(refAttr -> {
				EntityMetaData refEntityMeta = refAttr.getRefEntity();
				String refEntityName = refEntityMeta.getName();
				HugeSet<Object> refEntityIds = refEntitiesIds.get(refEntityName);
				if (refEntityIds == null)
				{
					refEntityIds = new HugeSet<>();
					refEntitiesIds.put(refEntityName, refEntityIds);

					Query q = new QueryImpl().fetch(new Fetch().field(refEntityMeta.getIdAttribute().getName()));
					for (Iterator<Entity> it = dataService.findAll(refEntityName, q).iterator(); it.hasNext();)
					{
						refEntityIds.add(it.next().getIdValue());
					}
				}
			});

			validationResource.setRefEntitiesIds(refEntitiesIds);
		}

		validationResource.setRefAttrs(refAttrs);
	}

	private void initUniqueValidation(ValidationResource validationResource)
	{
		// get unique attributes
		List<AttributeMetaData> uniqueAttrs = StreamSupport
				.stream(getEntityMetaData().getAtomicAttributes().spliterator(), false)
				.filter(attr -> attr.isUnique() && attr.getExpression() == null).collect(Collectors.toList());

		// get existing values for each attributes
		if (!uniqueAttrs.isEmpty())
		{
			Map<String, HugeMap<Object, Object>> uniqueAttrsValues = new HashMap<>();

			Fetch fetch = new Fetch();
			uniqueAttrs.forEach(uniqueAttr -> {
				uniqueAttrsValues.put(uniqueAttr.getName(), new HugeMap<>());
				fetch.field(uniqueAttr.getName());
			});

			Query q = new QueryImpl().fetch(fetch);
			decoratedRepository.findAll(q).forEach(entity -> {
				uniqueAttrs.forEach(uniqueAttr -> {
					HugeMap<Object, Object> uniqueAttrValues = uniqueAttrsValues.get(uniqueAttr.getName());
					Object attrValue = entity.get(uniqueAttr.getName());
					if (attrValue != null)
					{
						if (uniqueAttr.getDataType() instanceof XrefField)
						{
							attrValue = ((Entity) attrValue).getIdValue();
						}
						uniqueAttrValues.put(attrValue, entity.getIdValue());
					}
				});
			});

			validationResource.setUniqueAttrsValues(uniqueAttrsValues);
		}

		validationResource.setUniqueAttrs(uniqueAttrs);

	}

	private void initReadonlyValidation(ValidationResource validationResource)
	{
		List<AttributeMetaData> readonlyAttrs = StreamSupport
				.stream(getEntityMetaData().getAtomicAttributes().spliterator(), false)
				.filter(attr -> attr.isReadonly() && attr.getExpression() == null).collect(Collectors.toList());

		validationResource.setReadonlyAttrs(readonlyAttrs);
	}

	private void validateEntityValueRequired(Entity entity, ValidationResource validationResource)
	{
		validationResource.getRequiredValueAttrs().forEach(nonNillableAttr -> {
			Object value = entity.get(nonNillableAttr.getName());
			if (value == null || (nonNillableAttr.getDataType() instanceof MrefField
					&& !entity.getEntities(nonNillableAttr.getName()).iterator().hasNext()))
			{
				boolean isValid = false;

				// FIXME remove hack (see https://github.com/molgenis/molgenis/issues/4308)
				// Do not validate if Questionnaire status is not SUBMITTED
				if (EntityUtils.doesExtend(getEntityMetaData(), "Questionnaire")
						&& !"SUBMITTED".equals(entity.getString("status")))
				{
					isValid = true;
				}
				// Do not validate if visibleExpression resolves to false
				else if (nonNillableAttr.getVisibleExpression() != null && !expressionValidator
						.resolveBooleanExpression(nonNillableAttr.getVisibleExpression(), entity, getEntityMetaData()))
				{

					isValid = true;
				}

				if (!isValid)
				{
					String message = format("The attribute '%s' of entity '%s' can not be null.",
							nonNillableAttr.getName(), getName());

					ConstraintViolation constraintViolation = new ConstraintViolation(message, nonNillableAttr,
							Long.valueOf(validationResource.getRow()));
					validationResource.addViolation(constraintViolation);
				}
			}
		});
	}

	private void validateEntityValueTypes(Entity entity, ValidationResource validationResource)
	{
		// entity attributes validation
		Set<ConstraintViolation> attrViolations = entityAttributesValidator.validate(entity, getEntityMetaData());
		if (attrViolations != null && !attrViolations.isEmpty())
		{
			attrViolations.forEach(attrViolation -> {
				validationResource.addViolation(attrViolation);
			});
		}
	}

	private void validateEntityValueUniqueness(Entity entity, ValidationResource validationResource,
			ValidationMode validationMode)
	{
		validationResource.getUniqueAttrs().forEach(uniqueAttr -> {
			Object attrValue = entity.get(uniqueAttr.getName());
			if (attrValue != null)
			{
				if (uniqueAttr.getDataType() instanceof XrefField)
				{
					attrValue = ((Entity) attrValue).getIdValue();
				}

				HugeMap<Object, Object> uniqueAttrValues = validationResource.getUniqueAttrsValues()
						.get(uniqueAttr.getName());
				Object existingEntityId = uniqueAttrValues.get(attrValue);
				if ((validationMode == ValidationMode.ADD && existingEntityId != null)
						|| (validationMode == ValidationMode.UPDATE && existingEntityId != null
								&& !existingEntityId.equals(entity.getIdValue())))
				{
					ConstraintViolation constraintViolation = new ConstraintViolation(
							format("Duplicate value '%s' for unique attribute '%s' from entity '%s'", attrValue,
									uniqueAttr.getName(), getName()),
							uniqueAttr, Long.valueOf(validationResource.getRow()));
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
		validationResource.getRefAttrs().forEach(refAttr -> {
			HugeSet<Object> refEntityIds = validationResource.getRefEntitiesIds().get(refAttr.getRefEntity().getName());

			Iterable<Entity> refEntities;
			if (refAttr.getDataType() instanceof XrefField)
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
					boolean selfReference = entity.getEntityMetaData().getName()
							.equals(refAttr.getRefEntity().getName());
					if (!(selfReference && entity.getIdValue().equals(refEntity.getIdValue())))
					{
						String message = String.format("Unknown xref value '%s' for attribute '%s' of entity '%s'.",
								DataConverter.toString(refEntity.getIdValue()), refAttr.getName(), getName());

						ConstraintViolation constraintViolation = new ConstraintViolation(message, refAttr,
								Long.valueOf(validationResource.getRow()));
						validationResource.addViolation(constraintViolation);
					}
				}
				validationResource.addRefEntityId(getName(), refEntity.getIdValue());
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void validateEntityValueReadOnly(Entity entity, ValidationResource validationResource)
	{
		Entity entityToUpdate = findOne(entity.getIdValue());
		validationResource.getReadonlyAttrs().forEach(readonlyAttr -> {
			Object value = entity.get(readonlyAttr.getName());
			Object existingValue = entityToUpdate.get(readonlyAttr.getName());

			if (readonlyAttr.getDataType() instanceof XrefField)
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
			else if (readonlyAttr.getDataType() instanceof MrefField)
			{
				List<Object> entityIds = new ArrayList<>();
				((Iterable<Entity>) value).forEach(mrefEntity -> {
					entityIds.add(mrefEntity.getIdValue());
				});
				value = entityIds;

				List<Object> existingEntityIds = new ArrayList<>();
				((Iterable<Entity>) existingValue).forEach(mrefEntity -> {
					existingEntityIds.add(mrefEntity.getIdValue());
				});
				existingValue = existingEntityIds;
			}

			if (value != null && existingValue != null && !value.equals(existingValue))
			{
				validationResource
						.addViolation(
								new ConstraintViolation(
										format("The attribute '%s' of entity '%s' can not be changed it is readonly.",
												readonlyAttr.getName(), getName()),
										Long.valueOf(validationResource.getRow())));
			}
		});
	}

	/**
	 * Container with validation data used during stream validation
	 */
	private static class ValidationResource implements AutoCloseable
	{
		private AtomicInteger rowNr = new AtomicInteger();
		private List<AttributeMetaData> requiredValueAttrs;
		private List<AttributeMetaData> refAttrs;
		private Map<String, HugeSet<Object>> refEntitiesIds;
		private List<AttributeMetaData> uniqueAttrs;
		private Map<String, HugeMap<Object, Object>> uniqueAttrsValues;
		private List<AttributeMetaData> readonlyAttrs;
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

		public List<AttributeMetaData> getRequiredValueAttrs()
		{
			return requiredValueAttrs != null ? unmodifiableList(requiredValueAttrs) : emptyList();
		}

		public void setRequiredValueAttrs(List<AttributeMetaData> requiredValueAttrs)
		{
			this.requiredValueAttrs = requiredValueAttrs;
		}

		public List<AttributeMetaData> getRefAttrs()
		{
			return unmodifiableList(refAttrs);
		}

		public void setRefAttrs(List<AttributeMetaData> refAttrs)
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

		public List<AttributeMetaData> getUniqueAttrs()
		{
			return uniqueAttrs != null ? unmodifiableList(uniqueAttrs) : emptyList();
		}

		public void setUniqueAttrs(List<AttributeMetaData> uniqueAttrs)
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

		public List<AttributeMetaData> getReadonlyAttrs()
		{
			return readonlyAttrs != null ? unmodifiableList(readonlyAttrs) : emptyList();
		}

		public void setReadonlyAttrs(List<AttributeMetaData> readonlyAttrs)
		{
			this.readonlyAttrs = readonlyAttrs;
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
