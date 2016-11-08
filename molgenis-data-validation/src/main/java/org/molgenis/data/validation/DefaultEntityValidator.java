package org.molgenis.data.validation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
public class DefaultEntityValidator implements EntityValidator
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultEntityValidator.class);

	private final DataService dataService;
	private final EntityAttributesValidator entityAttributesValidator;

	@Autowired
	public DefaultEntityValidator(DataService dataService, EntityAttributesValidator entityAttributesValidator)
	{
		this.dataService = dataService;
		this.entityAttributesValidator = entityAttributesValidator;
	}

	@Override
	public void validate(Iterable<? extends Entity> entities, EntityType meta, DatabaseAction dbAction)
			throws MolgenisValidationException
	{
		Set<ConstraintViolation> violations = checkNotNull(entities, meta);
		violations.addAll(checkUniques(entities, meta, dbAction));

		long rownr = 0;
		for (Entity entity : entities)
		{
			rownr++;
			Set<ConstraintViolation> entityViolations = entityAttributesValidator.validate(entity, meta);
			for (ConstraintViolation entityViolation : entityViolations)
			{
				entityViolation.setRownr(rownr);
				violations.add(entityViolation);
			}
		}

		if (!violations.isEmpty())
		{
			LOG.info("Validation violations:" + violations);
			throw new MolgenisValidationException(violations);
		}
	}

	private Set<ConstraintViolation> checkNotNull(Iterable<? extends Entity> entities, EntityType meta)
	{
		Set<ConstraintViolation> violations = Sets.newLinkedHashSet();

		for (Attribute attr : meta.getAtomicAttributes())
		{
			if (!attr.isNillable() && !attr.equals(meta.getIdAttribute()) && !attr.isAuto())
			{
				long rownr = 0;
				for (Entity entity : entities)
				{
					rownr++;
					if (mustDoNotNullCheck(meta, attr, entity) && entity.get(attr.getName()) == null)
					{
						String message = String
								.format("The attribute '%s' of entity '%s' with key '%s' can not be null.",
										attr.getName(), meta.getName(),
										entity.getString(meta.getLabelAttribute().getName()));
						violations.add(new ConstraintViolation(message, null, entity, attr, meta, rownr));
					}
				}
			}
		}

		return violations;
	}

	public boolean mustDoNotNullCheck(EntityType entityType, Attribute attr, Entity entity)
	{
		// Do not validate is visibleExpression resolves to false
		if (StringUtils.isNotBlank(attr.getVisibleExpression()) && !ValidationUtils
				.resolveBooleanExpression(attr.getVisibleExpression(), entity, entityType)) return false;

		return true;
	}

	private Set<ConstraintViolation> checkUniques(Iterable<? extends Entity> entities, EntityType meta,
			DatabaseAction dbAction)
	{
		Set<ConstraintViolation> violations = Sets.newLinkedHashSet();

		for (Attribute attr : meta.getAtomicAttributes())
		{
			if (attr.isUnique() && !attr.equals(meta.getIdAttribute()) && !(attr.equals(meta.getLabelAttribute()) && (
					dbAction == DatabaseAction.ADD_UPDATE_EXISTING)))
			{
				// Gather all attribute values
				// FIXME: keeping everything in memory is not scaleable
				List<Object> values = Lists.newArrayList();
				for (Entity entity : entities)
				{
					Object value = entity.get(attr.getName());
					values.add(value);
				}

				// Create 'in' query, should find only find itself or nothing
				if (!values.isEmpty())
				{
					String entityName = meta.getName();

					long count = dataService.count(entityName, new QueryImpl<Entity>().in(attr.getName(), values));
					if (count > 0)
					{
						// Go through the list to find the violators
						long found = 0;
						Iterator<? extends Entity> it = entities.iterator();

						while (it.hasNext() && (found < count))
						{
							Entity entity = it.next();

							Object value = entity.get(attr.getName());
							Entity existing = dataService
									.findOne(entityName, new QueryImpl<Entity>().eq(attr.getName(), value));

							if (existing != null)
							{
								// If dbAction is null check on id if its an update of itself or a new insert. Violation
								// if dbAction is ADD
								if (((dbAction == null) && !idEquals(entity, existing, meta)) || ((dbAction != null)
										&& (dbAction == DatabaseAction.ADD)))
								{
									String message = String
											.format("The attribute '%s' of entity '%s' with key '%s' must be unique, but the value '%s' already exists.",
													attr.getName(), meta.getName(),
													entity.getString(meta.getLabelAttribute().getName()), value);
									violations.add(new ConstraintViolation(message, value, entity, attr, meta, null));
								}

								found++;
							}
						}
					}
				}

			}
		}

		return violations;
	}

	// Check is two entities have the same id (pk)
	private boolean idEquals(Entity e1, Entity e2, EntityType meta)
	{
		if (meta.getIdAttribute() != null)
		{
			String id1 = e1.getString(meta.getIdAttribute().getName());
			String id2 = e2.getString(meta.getIdAttribute().getName());

			if ((id1 != null) && (id2 != null) && id1.equals(id2))
			{
				return true;
			}
		}

		return false;
	}
}
