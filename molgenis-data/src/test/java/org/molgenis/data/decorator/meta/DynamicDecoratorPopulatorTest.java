package org.molgenis.data.decorator.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.PARAMETERS;
import static org.molgenis.data.decorator.meta.DynamicDecoratorMetadata.DYNAMIC_DECORATOR;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorRegistry;
import org.molgenis.test.AbstractMockitoTest;

class DynamicDecoratorPopulatorTest extends AbstractMockitoTest {
  @Mock private DynamicRepositoryDecoratorRegistry registry;
  @Mock private DataService dataService;
  @Mock private DynamicDecorator existingDecorator;
  @Mock private DynamicDecorator removedDecorator;
  @Mock private DynamicDecorator newDecorator;
  @Mock private DynamicRepositoryDecoratorFactory dynamicRepositoryDecoratorFactory;
  @Mock private DynamicDecoratorFactory dynamicDecoratorFactory;

  @Captor private ArgumentCaptor<Stream<DecoratorParameters>> parameterCaptor;

  private DynamicDecoratorPopulator populator;

  @BeforeEach
  void beforeMethod() {
    populator = new DynamicDecoratorPopulator(dataService, registry, dynamicDecoratorFactory);
  }

  @Test
  void testPopulate() {
    when(existingDecorator.getId()).thenReturn("id1");
    when(removedDecorator.getId()).thenReturn("id2");
    when(newDecorator.getId()).thenReturn("id3");

    when(newDecorator.setLabel(any())).thenReturn(newDecorator);
    when(newDecorator.setDescription(any())).thenReturn(newDecorator);
    when(newDecorator.setSchema(any())).thenReturn(newDecorator);

    when(registry.getFactoryIds()).thenAnswer(invocation -> Stream.of("id1", "id3"));
    when(registry.getFactory("id3")).thenReturn(dynamicRepositoryDecoratorFactory);
    when(dataService.findAll(DYNAMIC_DECORATOR, DynamicDecorator.class))
        .thenAnswer(invocation -> Stream.of(existingDecorator, removedDecorator));

    when(dataService.findOneById(DYNAMIC_DECORATOR, "id1", DynamicDecorator.class))
        .thenReturn(existingDecorator);

    when(dynamicDecoratorFactory.create("id3")).thenReturn(newDecorator);
    when(dynamicRepositoryDecoratorFactory.getLabel()).thenReturn("label3");
    when(dynamicRepositoryDecoratorFactory.getDescription()).thenReturn("desc3");

    populator.populate();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<DynamicDecorator>> decoratorCaptor = forClass(Stream.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> stringCaptor = forClass(Stream.class);

    verify(dataService).add(eq(DYNAMIC_DECORATOR), decoratorCaptor.capture());
    verify(dataService).deleteAll(eq(DYNAMIC_DECORATOR), stringCaptor.capture());
    List<DynamicDecorator> addedDecorators = decoratorCaptor.getValue().collect(toList());
    List<Object> deletedDecorators = stringCaptor.getValue().collect(toList());
    assertEquals(1, addedDecorators.size());
    assertEquals("id3", addedDecorators.get(0).getId());
    assertEquals(1, deletedDecorators.size());
    assertEquals("id2", deletedDecorators.get(0));
  }

  @Test
  void testRemoveReferences() {
    List<Object> idsToRemove = asList("id1", "id3");
    DecoratorConfiguration config = mock(DecoratorConfiguration.class);
    DecoratorParameters params1 = mock(DecoratorParameters.class);
    DecoratorParameters params2 = mock(DecoratorParameters.class);
    when(params1.getId()).thenReturn("id1");
    when(params2.getId()).thenReturn("id2");
    when(config.getEntities(PARAMETERS, DecoratorParameters.class))
        .thenReturn(asList(params1, params2));

    populator.removeReferencesOrDeleteIfEmpty(idsToRemove, config);

    verify(config).setDecoratorParameters(parameterCaptor.capture());
    assertEquals(singletonList(params2), parameterCaptor.getValue().collect(toList()));
  }

  @Test
  void testRemoveReferenceAndDeleteConfiguration() {
    List<Object> idsToRemove = asList("id1", "id2", "id3");
    DecoratorConfiguration config = mock(DecoratorConfiguration.class);
    when(config.getIdValue()).thenReturn("configId");
    DecoratorParameters params1 = mock(DecoratorParameters.class);
    DecoratorParameters params2 = mock(DecoratorParameters.class);
    when(params1.getId()).thenReturn("id1");
    when(params2.getId()).thenReturn("id2");
    when(config.getEntities(PARAMETERS, DecoratorParameters.class))
        .thenReturn(asList(params1, params2));

    DecoratorConfiguration returnedConfig =
        populator.removeReferencesOrDeleteIfEmpty(idsToRemove, config);

    verify(dataService).deleteById(DECORATOR_CONFIGURATION, "configId");
    assertNull(returnedConfig);
  }
}
