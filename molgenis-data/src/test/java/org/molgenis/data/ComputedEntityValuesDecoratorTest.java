package org.molgenis.data;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ComputedEntityValuesDecoratorTest
{
	private Repository decoratedRepo;
	private ComputedEntityValuesDecorator computedEntityValuesDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		computedEntityValuesDecorator = new ComputedEntityValuesDecorator(decoratedRepo);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(computedEntityValuesDecorator.add(entities), Integer.valueOf(123));
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.of(mock(Entity.class));
		computedEntityValuesDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Entity entity0 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepo).update(captor.capture());
		computedEntityValuesDecorator.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0));
	}
}
