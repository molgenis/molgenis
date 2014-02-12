package org.molgenis.data.validation;

import java.util.List;

import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

import com.google.common.collect.Lists;

public class EntityAttributesValidator implements EntityValidator
{
	private EmailValidator emailValidator;

	@Override
	public void validate(Iterable<? extends Entity> entities, EntityMetaData meta) throws MolgenisValidationException
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

		if (!violations.isEmpty())
		{
			throw new MolgenisValidationException(violations);
		}
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

}
