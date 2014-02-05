package org.molgenis.data.validation;

import java.util.Iterator;
import java.util.List;

import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class DefaultEntityValidator implements EntityValidator
{
	private final DataService dataService;
	private EmailValidator emailValidator;

	@Autowired
	public DefaultEntityValidator(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Override
	public void validate(Iterable<? extends Entity> entities, EntityMetaData meta) throws MolgenisValidationException
	{
		List<ConstraintViolation> violations = checkUniques(entities, meta);
		violations.addAll(checkDatatypes(entities, meta));

		if (!violations.isEmpty())
		{
			throw new MolgenisValidationException(violations);
		}
	}

	private List<ConstraintViolation> checkDatatypes(Iterable<? extends Entity> entities, EntityMetaData meta)
	{
		List<ConstraintViolation> violations = Lists.newArrayList();
		for (AttributeMetaData attr : meta.getAttributes())
		{
			for (Entity entity : entities)
			{
				ConstraintViolation violation = null;
				switch (attr.getDataType().getEnumType())
				{
					case EMAIL:
						violation = checkEmail(entity, attr, meta);
						break;
					case BOOL:
						violation = checkBoolean(entity, attr, meta);
						break;
					default:
						break;
				}

				if (violation != null)
				{
					violations.add(violation);
				}
			}
		}

		return violations;
	}

	private ConstraintViolation checkEmail(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		if (emailValidator == null)
		{
			emailValidator = new EmailValidator();
		}

		String email = entity.getString(attribute.getName());
		if (emailValidator.isValid(email, null))
		{
			return null;
		}

		String message = String.format("Invalid email value '%s'.", email);
		return new ConstraintViolation(message, email, entity, attribute, meta);
	}

	private ConstraintViolation checkBoolean(Entity entity, AttributeMetaData attribute, EntityMetaData meta)
	{
		try
		{
			entity.getBoolean(attribute.getName());
			return null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			String message = String.format("Invalid boolean value '%s'.", entity.getString(attribute.getName()));
			return new ConstraintViolation(message, entity.getString(attribute.getName()), entity, attribute, meta);
		}
	}

	private List<ConstraintViolation> checkUniques(Iterable<? extends Entity> entities, EntityMetaData meta)
	{
		List<ConstraintViolation> violations = Lists.newArrayList();

		for (AttributeMetaData attr : meta.getAttributes())
		{
			if (attr.isUnique())
			{
				// Gather all attribute values
				List<Object> values = Lists.newArrayList();
				for (Entity entity : entities)
				{
					Object value = entity.get(attr.getName());
					values.add(value);
				}

				// Create 'in' query, should find nothing in the repo
				if (!values.isEmpty())
				{
					long count = dataService.count(meta.getName(), new QueryImpl().in(attr.getName(), values));
					if (count > 0)
					{
						// Go through the list to find the violators
						long found = 0;
						Iterator<? extends Entity> it = entities.iterator();

						while (it.hasNext() && (found < count))
						{
							Entity entity = it.next();

							Object value = entity.get(attr.getName());
							Entity existing = dataService.findOne(meta.getName(),
									new QueryImpl().eq(attr.getName(), value));

							if ((existing != null) && !equals(entity, existing))
							{
								String message = String.format(
										"The attribute %s of %s must be unique, but the value %s already exists.",
										attr.getName(), meta.getName(), value);
								violations.add(new ConstraintViolation(message, value, entity, attr, meta));

								found++;
							}
						}
					}
				}

			}
		}

		return violations;
	}

	// Check id two entities
	private boolean equals(Entity e1, Entity e2)
	{
		if ((e1.getIdValue() != null) && (e2.getIdValue() != null))
		{
			if (e1.getIdValue().equals(e2.getIdValue()))
			{
				return true;
			}
		}

		if (e1.getLabelAttributeNames().equals(e2.getLabelAttributeNames()))
		{
			List<Object> values1 = Lists.newArrayList();
			for (String attrName : e1.getLabelAttributeNames())
			{
				values1.add(e1.get(attrName));
			}

			List<Object> values2 = Lists.newArrayList();
			for (String attrName : e2.getLabelAttributeNames())
			{
				values2.add(e2.get(attrName));
			}

			if (values1.equals(values2))
			{
				return true;
			}
		}

		return false;
	}
}
