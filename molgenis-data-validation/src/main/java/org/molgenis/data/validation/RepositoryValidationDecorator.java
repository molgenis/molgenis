package org.molgenis.data.validation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
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
	private final EntityAttributesValidator entityAttributesValidator;
	private final DataService dataService;
	private final Repository decoratedRepository;

	public RepositoryValidationDecorator(DataService dataService, Repository repository,
			EntityAttributesValidator entityAttributesValidator)
	{
		this.decoratedRepository = repository;
		this.dataService = dataService;
		this.entityAttributesValidator = entityAttributesValidator;
	}

	@Override
	public void update(Entity entity)
	{
		validate(Arrays.asList(entity), true);
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		validate(entities, true);
		decoratedRepository.update(entities);
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
	public Integer add(Iterable<? extends Entity> entities)
	{
		validate(entities, false);
		return decoratedRepository.add(entities);
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

	public boolean mustDoNotNullCheck(EntityMetaData entityMetaData, AttributeMetaData attr, Entity entity)
	{
		// Do not validate if Questionnaire status is not SUBMITTED
		if (EntityUtils.doesExtend(entityMetaData, "Questionnaire") && entity.get("status") != "SUBMITTED")
			return false;

		// Do not validate if visibleExpression resolves to false
		if (StringUtils.isNotBlank(attr.getVisibleExpression())
				&& !ValidationUtils.resolveBooleanExpression(attr.getVisibleExpression(), entity, entityMetaData))
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
			for (Entity refEntity : dataService.findAll(attr.getRefEntity().getName()))
			{
				refEntityIdValues.add(refEntity.getIdValue());
			}

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
	public Iterable<Entity> findAll(Query q)
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
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
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
}
