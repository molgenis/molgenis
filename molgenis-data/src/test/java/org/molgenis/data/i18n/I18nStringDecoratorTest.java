package org.molgenis.data.i18n;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.stream.Stream;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class I18nStringDecoratorTest
{
	private Repository decoratedRepo;
	private I18nStringDecorator i18nStringDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		i18nStringDecorator = new I18nStringDecorator(decoratedRepo);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(i18nStringDecorator.add(entities), Integer.valueOf(123));
	}
}
