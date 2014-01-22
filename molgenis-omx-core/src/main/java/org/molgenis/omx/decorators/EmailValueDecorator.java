package org.molgenis.omx.decorators;

import javax.validation.ValidationException;

import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.omx.observ.value.EmailValue;

public class EmailValueDecorator<E extends Entity> extends CrudRepositoryDecorator<E>
{
	private final EmailValidator emailValidator;

	public EmailValueDecorator(CrudRepository<E> generatedRepository)
	{
		super(generatedRepository);
		emailValidator = new EmailValidator();
	}

	@Override
	public void add(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
		{
			String email = entity.getString(EmailValue.VALUE);
			if (!emailValidator.isValid(email, null))
			{
				throw new ValidationException("not an email address [" + email + "]");
			}
		}

		super.add(entities);
	}

	@Override
	public Integer add(Entity entity)
	{
		String email = entity.getString(EmailValue.VALUE);
		if (!emailValidator.isValid(email, null))
		{
			throw new ValidationException("not an email address [" + email + "]");
		}

		return super.add(entity);
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
