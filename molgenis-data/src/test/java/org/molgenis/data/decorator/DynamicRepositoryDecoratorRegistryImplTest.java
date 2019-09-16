package org.molgenis.data.decorator;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.ENTITY_TYPE_ID;
import static org.molgenis.data.event.BootstrappingEvent.BootstrappingStatus.FINISHED;

import com.google.gson.Gson;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.meta.DecoratorConfiguration;
import org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata;
import org.molgenis.data.decorator.meta.DecoratorPackage;
import org.molgenis.data.decorator.meta.DecoratorParameters;
import org.molgenis.data.decorator.meta.DecoratorParametersMetadata;
import org.molgenis.data.decorator.meta.DynamicDecorator;
import org.molgenis.data.decorator.meta.DynamicDecoratorMetadata;
import org.molgenis.data.event.BootstrappingEvent;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      DecoratorConfigurationMetadata.class,
      DecoratorPackage.class,
      DynamicDecoratorMetadata.class,
      DecoratorParametersMetadata.class
    })
class DynamicRepositoryDecoratorRegistryImplTest extends AbstractMolgenisSpringTest {
  @Autowired DecoratorConfigurationMetadata decoratorConfigurationMetadata;

  @Autowired DecoratorParametersMetadata decoratorParametersMetadata;

  @Mock private Repository<Entity> repository;

  @Mock private DataService dataService;

  @Mock private EntityType entityType;
  @Mock private DecoratorConfiguration decoratorConfiguration;
  @Mock private DynamicRepositoryDecoratorFactory<Entity> dynamicRepositoryDecoratorFactory;

  private DynamicRepositoryDecoratorRegistryImpl registry;

  @BeforeEach
  void beforeMethod() {
    registry = new DynamicRepositoryDecoratorRegistryImpl(dataService, new Gson());

    // fake the bootstrapping event to tell the registry that bootstrapping is finished.
    registry.onApplicationEvent(new BootstrappingEvent(FINISHED));
  }

  @Test
  void testAddFactory() {
    DynamicRepositoryDecoratorFactory factory1 = mock(DynamicRepositoryDecoratorFactory.class);
    when(factory1.getId()).thenReturn("test1");
    DynamicRepositoryDecoratorFactory factory2 = mock(DynamicRepositoryDecoratorFactory.class);
    when(factory2.getId()).thenReturn("test2");

    registry.addFactory(factory1);
    registry.addFactory(factory2);

    assertEquals(newHashSet("test1", "test2"), registry.getFactoryIds().collect(toSet()));
  }

  @Test
  void testAddDuplicateFactory() {
    DynamicRepositoryDecoratorFactory factory = mock(DynamicRepositoryDecoratorFactory.class);
    when(factory.getId()).thenReturn("test");

    registry.addFactory(factory);
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> registry.addFactory(factory));
    assertThat(exception.getMessage()).containsPattern("Duplicate decorator id \\[test\\]");
  }

  @Test
  void testGetFactory() {
    DynamicRepositoryDecoratorFactory factory = mock(DynamicRepositoryDecoratorFactory.class);
    when(factory.getId()).thenReturn("test");
    registry.addFactory(factory);

    DynamicRepositoryDecoratorFactory returnedFactory = registry.getFactory("test");

    assertEquals(factory, returnedFactory);
  }

  @Test
  void testGetNonExistingFactory() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> registry.getFactory("test"));
    assertThat(exception.getMessage()).containsPattern("Decorator \\[test\\] does not exist");
  }

  @Test
  @SuppressWarnings("unchecked")
  void testDecorate() {
    when(entityType.getId()).thenReturn("entityTypeId");
    when(repository.getEntityType()).thenReturn(entityType);

    DynamicDecorator dynamicDecorator = mock(DynamicDecorator.class);
    DecoratorParameters parameters = mock(DecoratorParameters.class);
    when(parameters.getDecorator()).thenReturn(dynamicDecorator);
    Repository<Entity> decoratedRepository = mock(Repository.class);

    when(decoratedRepository.getName()).thenReturn("decoratedRepositoryName");
    when(decoratorConfiguration.getDecoratorParameters())
        .thenAnswer((Answer<Stream>) invocation -> Stream.of(parameters));

    when(dynamicDecorator.getId()).thenReturn("dynamicDecoratorId");

    when(dynamicRepositoryDecoratorFactory.getId()).thenReturn("dynamicDecoratorId");
    when(dynamicRepositoryDecoratorFactory.createDecoratedRepository(
            eq(repository), any(Map.class)))
        .thenReturn(decoratedRepository);
    Query query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(DECORATOR_CONFIGURATION, DecoratorConfiguration.class))
        .thenReturn(query);
    when(query.eq(ENTITY_TYPE_ID, "entityTypeId").findOne()).thenReturn(decoratorConfiguration);

    registry.addFactory(dynamicRepositoryDecoratorFactory);

    assertEquals("decoratedRepositoryName", registry.decorate(repository).getName());
  }

  @Test
  void testDecorateNoDecorator() {
    when(repository.getEntityType()).thenReturn(entityType);
    when(repository.getName()).thenReturn("repositoryName");
    when(entityType.getId()).thenReturn("entityTypeId");

    @SuppressWarnings("unchecked")
    Query<DecoratorConfiguration> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(DECORATOR_CONFIGURATION, DecoratorConfiguration.class))
        .thenReturn(query);
    when(query.eq(ENTITY_TYPE_ID, "entityTypeId").findOne()).thenReturn(null);

    assertEquals("repositoryName", registry.decorate(repository).getName());
  }

  @Test
  void getParameterMap() {
    DecoratorConfiguration config = mock(DecoratorConfiguration.class);
    DecoratorParameters params1 = mock(DecoratorParameters.class, Mockito.RETURNS_DEEP_STUBS);
    DecoratorParameters params2 = mock(DecoratorParameters.class, Mockito.RETURNS_DEEP_STUBS);
    when(config.getDecoratorParameters()).thenReturn(Stream.of(params1, params2));
    when(params1.getParameters()).thenReturn("{attr: 'test'}");
    when(params2.getParameters()).thenReturn("{column: 'test', value: 'text'}");
    when(params1.getDecorator().getId()).thenReturn("dec1");
    when(params2.getDecorator().getId()).thenReturn("dec2");
    Map<String, Map<String, Object>> expected =
        of("dec1", of("attr", "test"), "dec2", of("column", "test", "value", "text"));

    Map<String, Map<String, Object>> parameterMap = registry.getParameterMap(config);

    assertEquals(expected, parameterMap);
  }

  @Test
  void getParametersMapWithNullValue() {
    DecoratorConfiguration config = mock(DecoratorConfiguration.class);
    DecoratorParameters params1 = mock(DecoratorParameters.class, Mockito.RETURNS_DEEP_STUBS);
    DecoratorParameters params2 = mock(DecoratorParameters.class, Mockito.RETURNS_DEEP_STUBS);
    when(config.getDecoratorParameters()).thenReturn(Stream.of(params1, params2));
    when(params1.getParameters()).thenReturn("{attr: 'test'}");
    when(params2.getParameters()).thenReturn(null);
    when(params1.getDecorator().getId()).thenReturn("dec1");
    when(params2.getDecorator().getId()).thenReturn("dec2");
    Map<String, Map<String, Object>> expected = of("dec1", of("attr", "test"), "dec2", emptyMap());

    Map<String, Map<String, Object>> parameterMap = registry.getParameterMap(config);

    assertEquals(expected, parameterMap);
  }
}
