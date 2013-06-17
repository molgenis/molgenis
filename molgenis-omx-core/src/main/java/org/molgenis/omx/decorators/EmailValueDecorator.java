package org.molgenis.omx.decorators;

import java.util.List;

import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.omx.observ.value.EmailValue;

public class EmailValueDecorator<E extends EmailValue> extends MapperDecorator<E>
{
	private EmailValidator emailValidator;

	public EmailValueDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
		if (generatedMapper == null) throw new IllegalArgumentException("Mapper is null");
		emailValidator = new EmailValidator();
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		for (E entity : entities)
		{
			String email = entity.getValue();
			if (!emailValidator.isValid(email, null))
			{
				throw new DatabaseException("not an email address [" + email + "]");
			}
		}
		return super.add(entities);
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		for (E entity : entities)
		{
			String email = entity.getValue();
			if (!emailValidator.isValid(email, null))
			{
				throw new DatabaseException("not an email address [" + email + "]");
			}
		}
		return super.update(entities);
	}
}
