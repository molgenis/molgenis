package org.molgenis.omx.decorators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.omx.observ.value.HyperlinkValue;
import org.testng.annotations.Test;

public class HyperlinkValueDecoratorTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void HyperlinkValueDecorator()
	{
		new HyperlinkValueDecorator<HyperlinkValue>(null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void add() throws DatabaseException
	{
		Mapper<HyperlinkValue> mapper = mock(Mapper.class);
		HyperlinkValue value1 = new HyperlinkValue();
		value1.setValue("http://www.a1.org/");
		HyperlinkValue value2 = new HyperlinkValue();
		value2.setValue("http://www.a2.org/");
		new HyperlinkValueDecorator<HyperlinkValue>(mapper).add(Arrays.asList(value1, value2));
		verify(mapper).add(Arrays.asList(value1, value2));
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = DatabaseException.class)
	public void add_InvalidHyperlink() throws DatabaseException
	{
		Mapper<HyperlinkValue> mapper = mock(Mapper.class);
		HyperlinkValue value1 = new HyperlinkValue();
		value1.setValue("not a hyperlink");
		new HyperlinkValueDecorator<HyperlinkValue>(mapper).add(Arrays.asList(value1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void update() throws DatabaseException
	{
		Mapper<HyperlinkValue> mapper = mock(Mapper.class);
		HyperlinkValue value1 = new HyperlinkValue();
		value1.setValue("http://www.a1.org/");
		HyperlinkValue value2 = new HyperlinkValue();
		value2.setValue("http://www.a2.org/");
		new HyperlinkValueDecorator<HyperlinkValue>(mapper).update(Arrays.asList(value1, value2));
		verify(mapper).update(Arrays.asList(value1, value2));
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = DatabaseException.class)
	public void update_InvalidHyperlink() throws DatabaseException
	{
		Mapper<HyperlinkValue> mapper = mock(Mapper.class);
		HyperlinkValue value1 = new HyperlinkValue();
		value1.setValue("not a hyperlink");
		new HyperlinkValueDecorator<HyperlinkValue>(mapper).update(Arrays.asList(value1));
	}
}
