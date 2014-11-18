package org.molgenis.data.mysql;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.data.validation.MolgenisValidationException;

import com.google.common.collect.Sets;

/**
 * Validator for MsqlRepository
 */
public class MysqlEntityValidator implements EntityValidator
{
	private static Logger logger = Logger.getLogger(MysqlEntityValidator.class);
	private final DataService dataService;
	private final EntityAttributesValidator entityAttributesValidator;

	public MysqlEntityValidator(DataService dataService, EntityAttributesValidator entityAttributesValidator)
	{
		this.dataService = dataService;
		this.entityAttributesValidator = entityAttributesValidator;
	}

	@Override
	public void validate(Iterable<? extends Entity> entities, EntityMetaData meta, DatabaseAction dbAction)
			throws MolgenisValidationException
	{
		Set<ConstraintViolation> violations = checkNotNull(entities, meta);
		violations.addAll(checkIdValues(entities, meta, dbAction));

		if (dbAction != DatabaseAction.REMOVE && dbAction != DatabaseAction.REMOVE_IGNORE_MISSING
				&& !meta.getName().equalsIgnoreCase("attributes") && !meta.getName().equalsIgnoreCase("entities"))
		{
			violations.addAll(checkRefs(entities, meta));
		}

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
			logger.info("Validation violations:" + violations);
			throw new MolgenisValidationException(violations);
		}
	}

	protected Set<ConstraintViolation> checkRefs(Iterable<? extends Entity> entities, EntityMetaData meta)
	{
		Set<ConstraintViolation> violations = Sets.newLinkedHashSet();

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			if (attr.getRefEntity() != null)
			{
				Set<Object> refIds = Sets.newHashSet();
				for (Entity ref : dataService.findAll(attr.getRefEntity().getName()))
				{
					refIds.add(ref.getIdValue());
				}
				if (attr.getRefEntity().equals(meta))
				{
					// Self ref, add current entities
					for (Entity e : entities)
					{
						Object id = getId(e, meta);
						if (id != null)
						{
							refIds.add(id);
						}
					}
				}

				long rownr = 0;
				for (Entity entity : entities)
				{
					rownr++;
					List<Object> refs = DataConverter.toObjectList(entity.get(attr.getName()));
					if (refs != null)
					{
						for (Object ref : refs)
						{
							Object refId = (ref instanceof Entity) ? ((Entity) ref).getIdValue() : ref;
							if ((refId != null) && (refId instanceof String))
							{
								refId = StringUtils.trim((String) refId);
							}

							if (!refIds.contains(attr.getRefEntity().getIdAttribute().getDataType().convert(refId)))
							{
								String message = String.format(
										"The attribute '%s' of entity '%s' references a non existing '%s' id '%s'.",
										attr.getName(), meta.getName(), attr.getRefEntity().getName(), refId);
								violations.add(new ConstraintViolation(message, null, entity, attr, meta, rownr));
							}
						}
					}
				}

			}
		}

		return violations;
	}

	private Object getId(Entity entity, EntityMetaData meta)
	{
		return meta.getIdAttribute().getDataType().convert(entity.get(meta.getIdAttribute().getName()));
	}

	protected Set<ConstraintViolation> checkNotNull(Iterable<? extends Entity> entities, EntityMetaData meta)
	{
		Set<ConstraintViolation> violations = Sets.newLinkedHashSet();

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			if (!attr.isNillable() && !attr.isAuto() && (attr.getDefaultValue() == null))
			{
				long rownr = 0;
				for (Entity entity : entities)
				{
					rownr++;
					if (entity.get(attr.getName()) == null)
					{
						String message = String.format("The attribute '%s' of entity '%s' can not be null.",
								attr.getName(), meta.getName());
						violations.add(new ConstraintViolation(message, null, entity, attr, meta, rownr));
					}
				}
			}
		}

		return violations;
	}

	protected Set<ConstraintViolation> checkIdValues(Iterable<? extends Entity> entities, EntityMetaData meta,
			DatabaseAction dbAction)
	{
		Set<ConstraintViolation> violations = Sets.newLinkedHashSet();
		Set<Long> doubleRowNums = Sets.newHashSet();

		long rownr1 = 0;
		for (Entity entity1 : entities)
		{
			rownr1++;
			Object id1 = entity1.get(meta.getIdAttribute().getName());
			if (id1 != null)
			{
				long rownr2 = 0;
				for (Entity entity2 : entities)
				{
					rownr2++;
					if (rownr1 != rownr2 && !doubleRowNums.contains(rownr2))
					{
						Object id2 = entity2.get(meta.getIdAttribute().getName());
						if (id2 != null)
						{
							if (id1.equals(id2))
							{
								doubleRowNums.add(rownr1);
								doubleRowNums.add(rownr2);

								String message = String.format(
										"Duplicate id value of '%s' for entity %s on row %d and %d.", id1,
										meta.getName(), rownr1 + 1, rownr2 + 1);
								violations.add(new ConstraintViolation(message, null, entity1, meta.getIdAttribute(),
										meta, rownr2));
							}
						}
					}
				}
			}
		}

		return violations;
	}
}
