package org.molgenis.data.decorator;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.ENTITY_TYPE_ID;
import static org.molgenis.data.event.BootstrappingEvent.BootstrappingStatus.FINISHED;
import static org.testng.Assert.assertEquals;

import com.google.gson.Gson;
import java.util.Map;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.meta.*;
import org.molgenis.data.event.BootstrappingEvent;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      DecoratorConfigurationMetadata.class,
      DecoratorPackage.class,
      DynamicDecoratorMetadata.class,
      DecoratorParametersMetadata.class
    })
public class DynamicRepositoryDecoratorRegistryImplTest extends AbstractMolgenisSpringTest {
  @Autowired DecoratorConfigurationMetadata decoratorConfigurationMetadata;

  @Autowired DecoratorParametersMetadata decoratorParametersMetadata;

  @Mock private Repository<Entity> repository;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private DataService dataService;

  @Mock private EntityType entityType;
  @Mock private DecoratorConfiguration decoratorConfiguration;
  @Mock private DynamicRepositoryDecoratorFactory<Entity> dynamicRepositoryDecoratorFactory;

  private DynamicRepositoryDecoratorRegistryImpl registry;

  @BeforeMethod
  public void beforeMethod() {
    registry = new DynamicRepositoryDecoratorRegistryImpl(dataService, new Gson());

    // fake the bootstrapping event to tell the registry that bootstrapping is finished.
    registry.onApplicationEvent(new BootstrappingEvent(FINISHED));
  }

  @Test
  public void testAddFactory() {
    DynamicRepositoryDecoratorFactory factory1 = mock(DynamicRepositoryDecoratorFactory.class);
    when(factory1.getId()).thenReturn("test1");
    DynamicRepositoryDecoratorFactory factory2 = mock(DynamicRepositoryDecoratorFactory.class);
    when(factory2.getId()).thenReturn("test2");

    registry.addFactory(factory1);
    registry.addFactory(factory2);

    assertEquals(registry.getFactoryIds().collect(toSet()), newHashSet("test1", "test2"));
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Duplicate decorator id \\[test\\]")
  public void testAddDuplicateFactory() {
    DynamicRepositoryDecoratorFactory factory = mock(DynamicRepositoryDecoratorFactory.class);
    when(factory.getId()).thenReturn("test");

    registry.addFactory(factory);
    registry.addFactory(factory);
  }

  @Test
  public void testGetFactory() {
    DynamicRepositoryDecoratorFactory factory = mock(DynamicRepositoryDecoratorFactory.class);
    when(factory.getId()).thenReturn("test");
    registry.addFactory(factory);

    DynamicRepositoryDecoratorFactory returnedFactory = registry.getFactory("test");

    assertEquals(returnedFactory, factory);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Decorator \\[test\\] does not exist")
  public void testGetNonExistingFactory() {
    registry.getFactory("test");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDecorate() {
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
    when(dataService
            .query(DECORATOR_CONFIGURATION, DecoratorConfiguration.class)
            .eq(ENTITY_TYPE_ID, "entityTypeId")
            .findOne())
        .thenReturn(decoratorConfiguration);

    registry.addFactory(dynamicRepositoryDecoratorFactory);

    assertEquals(registry.decorate(repository).getName(), "decoratedRepositoryName");
  }

  @Test
  public void testDecorateNoDecorator() {
    when(repository.getEntityType()).thenReturn(entityType);
    when(repository.getName()).thenReturn("repositoryName");
    when(entityType.getId()).thenReturn("entityTypeId");

    when(dataService
            .query(DECORATOR_CONFIGURATION, DecoratorConfiguration.class)
            .eq(ENTITY_TYPE_ID, "entityTypeId")
            .findOne())
        .thenReturn(null);

    assertEquals(registry.decorate(repository).getName(), "repositoryName");
  }

  @Test
  public void getParameterMap() {
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

    assertEquals(parameterMap, expected);
  }

  @Test
  public void getParametersMapWithNullValue() {
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

    assertEquals(parameterMap, expected);
  }
}
