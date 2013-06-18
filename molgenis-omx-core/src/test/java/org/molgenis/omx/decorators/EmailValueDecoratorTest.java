package org.molgenis.omx.decorators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.omx.observ.value.EmailValue;
import org.testng.annotations.Test;

public class EmailValueDecoratorTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void EmailValueDecorator()
	{
		new EmailValueDecorator<EmailValue>(null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void add() throws DatabaseException
	{
		Mapper<EmailValue> mapper = mock(Mapper.class);
		EmailValue value1 = new EmailValue();
		value1.setValue("a1@b.org");
		EmailValue value2 = new EmailValue();
		value2.setValue("a2@b.org");
		new EmailValueDecorator<EmailValue>(mapper).add(Arrays.asList(value1, value2));
		verify(mapper).add(Arrays.asList(value1, value2));
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = DatabaseException.class)
	public void add_InvalidEmail() throws DatabaseException
	{
		Mapper<EmailValue> mapper = mock(Mapper.class);
		EmailValue value1 = new EmailValue();
		value1.setValue("not an email address");
		new EmailValueDecorator<EmailValue>(mapper).add(Arrays.asList(value1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void update() throws DatabaseException
	{
		Mapper<EmailValue> mapper = mock(Mapper.class);
		EmailValue value1 = new EmailValue();
		value1.setValue("a1@b.org");
		EmailValue value2 = new EmailValue();
		value2.setValue("a2@b.org");
		new EmailValueDecorator<EmailValue>(mapper).update(Arrays.asList(value1, value2));
		verify(mapper).update(Arrays.asList(value1, value2));
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = DatabaseException.class)
	public void update_InvalidEmail() throws DatabaseException
	{
		Mapper<EmailValue> mapper = mock(Mapper.class);
		EmailValue value1 = new EmailValue();
		value1.setValue("not an email address");
		new EmailValueDecorator<EmailValue>(mapper).update(Arrays.asList(value1));
	}
}
