package org.molgenis.omx.decorators;

import javax.validation.ValidationException;

import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.omx.observ.value.EmailValue;

public class EmailValueDecorator<E extends EmailValue> extends CrudRepositoryDecorator<E>
{
	private final EmailValidator emailValidator;

	public EmailValueDecorator(CrudRepository<E> generatedRepository)
	{
		super(generatedRepository);
		emailValidator = new EmailValidator();
	}

	@Override
	public void add(Iterable<E> entities)
	{
		for (EmailValue entity : entities)
		{
			String email = entity.getValue();
			if (!emailValidator.isValid(email, null))
			{
				throw new ValidationException("not an email address [" + email + "]");
			}
		}

		super.add(entities);
	}

	@Override
	public void add(E entity)
	{
		String email = entity.getValue();
		if (!emailValidator.isValid(email, null))
		{
			throw new ValidationException("not an email address [" + email + "]");
		}

		super.add(entity);
	}

	@Override
	public void update(E entity)
	{
		String email = entity.getValue();
		if (!emailValidator.isValid(email, null))
		{
			throw new ValidationException("not an email address [" + email + "]");
		}

		super.update(entity);
	}

	@Override
	public void update(Iterable<E> entities)
	{
		for (E entity : entities)
		{
			String email = entity.getValue();
			if (!emailValidator.isValid(email, null))
			{
				throw new ValidationException("not an email address [" + email + "]");
			}
		}

		super.update(entities);
	}

}
