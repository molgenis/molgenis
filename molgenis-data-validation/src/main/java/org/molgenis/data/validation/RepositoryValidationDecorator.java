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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.HugeMap;
import org.molgenis.util.HugeSet;

import com.google.common.collect.Sets;

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
		validate(Arrays.asList(entity), true);
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
		validate(Arrays.asList(entity), false);
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

			validateEntityValueRequired(entity, validationResource);

			validateEntityValueTypes(entity, validationResource);

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
				if (existingEntityId != null)
				{
					System.out.println(existingEntityId);
					System.out.println(entity.getIdValue());
				}
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

			if (readonlyAttr instanceof XrefField)
			{
				if (value != null)
				{
					value = ((Entity) value).getIdValue();
				}
				if (existingValue != null)
				{
					value = ((Entity) existingValue).getIdValue();
				}
			}
			else if (readonlyAttr instanceof MrefField)
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

	private void validate(Iterable<? extends Entity> entities, boolean forUpdate)
	{
		if (ENTITIES_THAT_DO_NOT_NEED_VALIDATION.contains(getName())) return;

		Set<ConstraintViolation> violations = null;
		for (Entity entity : entities)
		{
			violations = entityAttributesValidator.validate(entity, getEntityMetaData());
			if (!violations.isEmpty())
			{
				throw new MolgenisValidationException(violations);
			}
		}

		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			if (attr.isUnique() && !attr.isAuto())
			{
				violations = checkUniques(entities, attr, forUpdate);
				if (!violations.isEmpty())
				{
					throw new MolgenisValidationException(violations);
				}
			}

			if (!decoratedRepository.getName().equalsIgnoreCase("UserAuthority"))// FIXME MolgenisUserDecorator
																					// adds UserAuthority in add
																					// method so it is not yet
																					// indexed and can not be found
			{
				if (attr.getDataType() instanceof XrefField || attr.getDataType() instanceof MrefField)
				{
					if (attr.getExpression() != null)
					{
						continue;
					}
					violations = checkRefValues(entities, attr);
					if (!violations.isEmpty())
					{
						throw new MolgenisValidationException(violations);
					}
				}
			}
		}

		violations = checkNillable(entities);
		if (!violations.isEmpty())
		{
			throw new MolgenisValidationException(violations);
		}

		if (forUpdate)
		{
			violations = checkReadonlyByUpdate(entities);
			if (!violations.isEmpty())
			{
				throw new MolgenisValidationException(violations);
			}
		}

	}

	@SuppressWarnings("unchecked")
	protected Set<ConstraintViolation> checkNillable(Iterable<? extends Entity> entities)
	{
		Set<ConstraintViolation> violations = Sets.newHashSet();

		long rownr = 0;
		for (Entity entity : entities)
		{
			rownr++;
			for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
			{
				if (!attr.isNillable())
				{
					Object value = entity.get(attr.getName());
					if ((value == null || (attr.getDataType() instanceof MrefField
							&& !(((Iterable<Entity>) value).iterator().hasNext()))) && !attr.isAuto()
							&& mustDoNotNullCheck(getEntityMetaData(), attr, entity))
					{
						String message = String.format("The attribute '%s' of entity '%s' can not be null.",
								attr.getName(), getName());
						violations.add(new ConstraintViolation(message, attr, rownr));
						if (violations.size() > 4) return violations;
					}
				}
			}
		}

		return violations;
	}

	private boolean mustDoNotNullCheck(EntityMetaData entityMetaData, AttributeMetaData attr, Entity entity)
	{
		// FIXME remove hack (see https://github.com/molgenis/molgenis/issues/4308)
		// Do not validate if Questionnaire status is not SUBMITTED
		if (EntityUtils.doesExtend(entityMetaData, "Questionnaire") && entity.get("status") != "SUBMITTED")
			return false;

		// Do not validate if visibleExpression resolves to false
		if (StringUtils.isNotBlank(attr.getVisibleExpression())
				&& !expressionValidator.resolveBooleanExpression(attr.getVisibleExpression(), entity, entityMetaData))
			return false;

		return true;
	}

	protected Set<ConstraintViolation> checkReadonlyByUpdate(Iterable<? extends Entity> entities)
	{
		Set<ConstraintViolation> violations = Sets.newHashSet();

		long rownr = 0;
		for (Entity entity : entities)
		{
			rownr++;
			Entity oldEntity = this.findOne(entity.getIdValue());
			if (null == oldEntity)
			{
				String message = String.format("The original entity with id: '%s' does not exists",
						entity.getIdValue());
				violations.add(new ConstraintViolation(message, entity.getEntityMetaData().getIdAttribute(), rownr));
				if (violations.size() > 4) return violations;
			}
			else
			{
				for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
				{
					if (attr.isReadonly() && attr.getExpression() == null)
					{
						Object newValue = attr.getDataType().convert(entity.get(attr.getName()));
						Object oldValue = attr.getDataType().convert(oldEntity.get(attr.getName()));

						if (attr.getDataType() instanceof XrefField)
						{
							if (newValue != null) newValue = ((Entity) newValue).getIdValue();
							if (oldValue != null) oldValue = ((Entity) oldValue).getIdValue();
						}

						if (((null == newValue) && (null != oldValue))
								|| ((null != newValue) && !newValue.equals(oldValue)))
						{
							String message = String.format(
									"The attribute '%s' of entity '%s' can not be changed it is readonly.",
									attr.getName(), getEntityMetaData().getLabel());
							violations.add(new ConstraintViolation(message, attr, rownr));
							if (violations.size() > 4) return violations;
						}
					}
				}
			}
		}

		return violations;
	}

	protected Set<ConstraintViolation> checkRefValues(Iterable<? extends Entity> entities, AttributeMetaData attr)
	{
		Set<ConstraintViolation> violations = Sets.newHashSet();
		HugeSet<Object> refEntityIdValues = new HugeSet<Object>();

		try
		{
			dataService.findAll(attr.getRefEntity().getName()).forEach(refEntity -> {
				refEntityIdValues.add(refEntity.getIdValue());
			});

			if (attr.getRefEntity().getName().equalsIgnoreCase(getName()))// Self reference
			{
				for (Entity entity : entities)
				{
					if (entity.getIdValue() != null)
					{
						refEntityIdValues.add(entity.getIdValue());
					}
				}
			}

			long rownr = 0;
			for (Entity entity : entities)
			{
				rownr++;
				if (attr.getDataType() instanceof XrefField)
				{
					Entity refEntity = entity.getEntity(attr.getName());
					if ((refEntity != null) && (refEntity.getIdValue() != null)
							&& !refEntityIdValues.contains(refEntity.getIdValue()))
					{
						String message = String.format("Unknown xref value '%s' for attribute '%s' of entity '%s'.",
								DataConverter.toString(refEntity.getIdValue()), attr.getName(),
								getEntityMetaData().getLabel());
						violations.add(new ConstraintViolation(message, attr, rownr));
						if (violations.size() > 4) break;
					}
				}
				else if (attr.getDataType() instanceof MrefField)
				{
					Iterable<Entity> refEntities = entity.getEntities(attr.getName());
					if (refEntities != null)
					{
						for (Entity refEntity : refEntities)
						{
							if (refEntity == null)
							{
								String message = String.format("Unknown refEntity for attribute '%s' of entity '%s'.",
										attr.getName(), getEntityMetaData().getLabel());
								violations.add(new ConstraintViolation(message, attr, rownr));
								if (violations.size() > 4) break;
							}

							else if ((refEntity.getIdValue() != null)
									&& !refEntityIdValues.contains(refEntity.getIdValue()))
							{
								String message = String.format(
										"Unknown mref value '%s' for attribute '%s' of entity '%s'.",
										DataConverter.toString(refEntity.getIdValue()), attr.getName(),
										getEntityMetaData().getLabel());
								violations.add(new ConstraintViolation(message, attr, rownr));
								if (violations.size() > 4) break;
							}
						}
					}
				}
			}

			return violations;
		}
		finally
		{
			IOUtils.closeQuietly(refEntityIdValues);
		}
	}

	protected Set<ConstraintViolation> checkUniques(Iterable<? extends Entity> entities, AttributeMetaData attr,
			boolean forUpdate)
	{
		Set<ConstraintViolation> violations = Sets.newHashSet();
		HugeMap<Object, Object> values = new HugeMap<Object, Object>();// key:attribute-value value:attribute-id
		try
		{
			if (count() > 0)
			{
				// Get all attr values currently in the repository
				for (Entity entity : this)
				{
					Object value = entity.get(attr.getName());
					if ((value != null) && (entity.getIdValue() != null))
					{
						if (value instanceof Entity)
						{
							value = ((Entity) value).getIdValue();
						}
						values.put(value, entity.getIdValue());
					}
				}
			}

			// Check the new entities
			long rownr = 0;
			for (Entity entity : entities)
			{
				rownr++;
				Object value = entity.get(attr.getName());

				if (value != null)
				{
					if (value instanceof Entity)
					{
						value = ((Entity) value).getIdValue();
					}

					// If adding, values can never contain the value; if updating check if the id's are different, if
					// the
					// same, your updating that entity
					Object id = values.get(value);
					if (values.containsKey(value) && (!forUpdate || !entityHasId(entity, id)))
					{
						violations.add(new ConstraintViolation("Duplicate value '" + value + "' for unique attribute '"
								+ attr.getName() + "' from entity '" + getName() + "'", attr, rownr));
						if (violations.size() > 4) break;
					}
					else
					{
						if (entity.getIdValue() != null)
						{
							values.put(value, entity.getIdValue());
						}
					}
				}
			}

			return violations;
		}
		finally
		{
			IOUtils.closeQuietly(values);
		}
	}

	// Check if the id of an entity equals a given id
	private boolean entityHasId(Entity entity, Object id)
	{
		FieldType idDataType = getEntityMetaData().getIdAttribute().getDataType();
		return idDataType.convert(id).equals(idDataType.convert(entity.getIdValue()));
	}
}
