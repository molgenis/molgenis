package org.molgenis.data.decorator.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorRegistry;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.molgenis.data.decorator.meta.DynamicDecoratorMetadata.DYNAMIC_DECORATOR;
import static org.testng.Assert.assertEquals;

public class DynamicDecoratorPopulatorTest
{

	private DynamicRepositoryDecoratorRegistry registry;
	private DataService dataService;
	private DynamicDecorator decorator1;
	private DynamicDecorator decorator2;
	private DynamicDecorator decorator3;

	@Test
	public void testPopulate()
	{
		registry = mock(DynamicRepositoryDecoratorRegistry.class);
		dataService = mock(DataService.class);

		decorator1 = mock(DynamicDecorator.class);
		decorator2 = mock(DynamicDecorator.class);
		decorator3 = mock(DynamicDecorator.class);
		when(decorator1.getId()).thenReturn("id1");
		when(decorator2.getId()).thenReturn("id2");
		when(decorator3.getId()).thenReturn("id3");
		DynamicDecoratorFactory dynamicDecoratorFactory = mock(DynamicDecoratorFactory.class);

		when(registry.getFactoryIds()).thenAnswer(invocation -> Stream.of("id1", "id3"));
		when(dataService.findAll(DYNAMIC_DECORATOR, DynamicDecorator.class)).thenAnswer(
				invocation -> Stream.of(decorator1, decorator2));

		when(dataService.findOneById(DYNAMIC_DECORATOR, "id1", DynamicDecorator.class)).thenReturn(decorator1);
		when(dataService.findOneById(DYNAMIC_DECORATOR, "id2", DynamicDecorator.class)).thenReturn(decorator2);

		when(dynamicDecoratorFactory.create("id3")).thenReturn(decorator3);

		DynamicDecoratorPopulator populator = new DynamicDecoratorPopulator(dataService, registry,
				dynamicDecoratorFactory);
		populator.populate();

		ArgumentCaptor<Stream<DynamicDecorator>> decoratorCaptor = forClass(Stream.class);
		ArgumentCaptor<Stream<Object>> stringCaptor = forClass(Stream.class);

		verify(dataService).add(eq(DYNAMIC_DECORATOR), decoratorCaptor.capture());
		verify(dataService).deleteAll(eq(DYNAMIC_DECORATOR), stringCaptor.capture());
		List<DynamicDecorator> addedDecorators = decoratorCaptor.getValue().collect(Collectors.toList());
		List<Object> deletedDecorators = stringCaptor.getValue().collect(Collectors.toList());
		assertEquals(addedDecorators.size(), 1);
		assertEquals(addedDecorators.get(0).getId(), "id3");
		assertEquals(deletedDecorators.size(), 1);
		assertEquals(deletedDecorators.get(0), "id2");
	}
}