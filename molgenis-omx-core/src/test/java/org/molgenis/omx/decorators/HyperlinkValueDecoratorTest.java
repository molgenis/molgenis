package org.molgenis.omx.decorators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import javax.validation.ValidationException;

import org.molgenis.data.CrudRepository;
import org.molgenis.omx.observ.value.HyperlinkValue;
import org.testng.annotations.Test;

public class HyperlinkValueDecoratorTest
{
	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void HyperlinkValueDecorator()
	{
		new HyperlinkValueDecorator(null);
	}

	@SuppressWarnings("resource")
	@Test
	public void add()
	{
		CrudRepository mapper = mock(CrudRepository.class);
		HyperlinkValue value1 = new HyperlinkValue();
		value1.setValue("http://www.a1.org/");
		HyperlinkValue value2 = new HyperlinkValue();
		value2.setValue("http://www.a2.org/");
		new HyperlinkValueDecorator(mapper).add(Arrays.asList(value1, value2));
		verify(mapper).add(Arrays.asList(value1, value2));
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = ValidationException.class)
	public void add_InvalidHyperlink()
	{
		CrudRepository mapper = mock(CrudRepository.class);
		HyperlinkValue value1 = new HyperlinkValue();
		value1.setValue("not a hyperlink");
		new HyperlinkValueDecorator(mapper).add(Arrays.asList(value1));
	}

	@SuppressWarnings("resource")
	@Test
	public void update()
	{
		CrudRepository mapper = mock(CrudRepository.class);
		HyperlinkValue value1 = new HyperlinkValue();
		value1.setValue("http://www.a1.org/");
		HyperlinkValue value2 = new HyperlinkValue();
		value2.setValue("http://www.a2.org/");
		new HyperlinkValueDecorator(mapper).update(Arrays.asList(value1, value2));
		verify(mapper).update(Arrays.asList(value1, value2));
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = ValidationException.class)
	public void update_InvalidHyperlink()
	{
		CrudRepository mapper = mock(CrudRepository.class);
		HyperlinkValue value1 = new HyperlinkValue();
		value1.setValue("not a hyperlink");
		new HyperlinkValueDecorator(mapper).update(Arrays.asList(value1));
	}
}
