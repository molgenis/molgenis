package org.molgenis.omx.decorators;

import javax.validation.ValidationException;

import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.omx.observ.value.EmailValue;

public class EmailValueDecorator extends CrudRepositoryDecorator
{
	private final EmailValidator emailValidator;

	public EmailValueDecorator(CrudRepository generatedRepository)
	{
		super(generatedRepository);
		emailValidator = new EmailValidator();
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
		{
			String email = entity.getString(EmailValue.VALUE);
			if (!emailValidator.isValid(email, null))
			{
				throw new ValidationException("not an email address [" + email + "]");
			}
		}

		return super.add(entities);
	}

	@Override
	public void add(Entity entity)
	{
		String email = entity.getString(EmailValue.VALUE);
		if (!emailValidator.isValid(email, null))
		{
			throw new ValidationException("not an email address [" + email + "]");
		}

		super.add(entity);
	}

	@Override
	public void update(Entity entity)
	{
		String email = entity.getString(EmailValue.VALUE);
		if (!emailValidator.isValid(email, null))
		{
			throw new ValidationException("not an email address [" + email + "]");
		}

		super.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
		{
			String email = entity.getString(EmailValue.VALUE);
			if (!emailValidator.isValid(email, null))
			{
				throw new ValidationException("not an email address [" + email + "]");
			}
		}

		super.update(entities);
	}

}
