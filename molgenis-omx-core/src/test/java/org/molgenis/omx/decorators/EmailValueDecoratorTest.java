package org.molgenis.omx.decorators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import javax.validation.ValidationException;

import org.molgenis.data.CrudRepository;
import org.molgenis.omx.observ.value.EmailValue;
import org.testng.annotations.Test;

public class EmailValueDecoratorTest
{
	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void EmailValueDecorator()
	{
		new EmailValueDecorator(null);
	}

	@SuppressWarnings("resource")
	@Test
	public void add()
	{
		CrudRepository mapper = mock(CrudRepository.class);
		EmailValue value1 = new EmailValue();
		value1.setValue("a1@b.org");
		EmailValue value2 = new EmailValue();
		value2.setValue("a2@b.org");
		new EmailValueDecorator(mapper).add(Arrays.asList(value1, value2));
		verify(mapper).add(Arrays.asList(value1, value2));
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = ValidationException.class)
	public void add_InvalidEmail()
	{
		CrudRepository mapper = mock(CrudRepository.class);
		EmailValue value1 = new EmailValue();
		value1.setValue("not an email address");
		new EmailValueDecorator(mapper).add(Arrays.asList(value1));
	}

	@SuppressWarnings("resource")
	@Test
	public void update()
	{
		CrudRepository mapper = mock(CrudRepository.class);
		EmailValue value1 = new EmailValue();
		value1.setValue("a1@b.org");
		EmailValue value2 = new EmailValue();
		value2.setValue("a2@b.org");
		new EmailValueDecorator(mapper).update(Arrays.asList(value1, value2));
		verify(mapper).update(Arrays.asList(value1, value2));
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = ValidationException.class)
	public void update_InvalidEmail()
	{
		CrudRepository mapper = mock(CrudRepository.class);
		EmailValue value1 = new EmailValue();
		value1.setValue("not an email address");
		new EmailValueDecorator(mapper).update(Arrays.asList(value1));
	}
}
