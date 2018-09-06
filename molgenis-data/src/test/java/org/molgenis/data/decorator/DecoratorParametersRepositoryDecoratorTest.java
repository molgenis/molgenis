package org.molgenis.data.decorator;

import static org.mockito.Mockito.*;

import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.meta.DecoratorParameters;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.validation.JsonValidator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DecoratorParametersRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private JsonValidator jsonValidator;

  @Mock private Repository delegateRepo;

  @Captor private ArgumentCaptor<Stream<DecoratorParameters>> streamCaptor;

  private DecoratorParametersRepositoryDecorator decorator;

  private static final String SCHEMA = "{some: 'schema'}";
  private static final String JSON = "{para: 'meters'}";

  @BeforeMethod
  @SuppressWarnings("unchecked")
  public void beforeMethod() {
    decorator = new DecoratorParametersRepositoryDecorator(delegateRepo, jsonValidator);
  }

  @Test
  public void testUpdate() {
    decorator.update(mockParameters());
    verify(jsonValidator).validate(JSON, SCHEMA);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateStream() {
    decorator.update(Stream.of(mockParameters(), mockParameters()));

    verify(delegateRepo).update(streamCaptor.capture());
    streamCaptor
        .getValue()
        .forEach(
            t -> { // consume stream
            });
    verify(jsonValidator, times(2)).validate(JSON, SCHEMA);
  }

  @Test
  public void testAdd() {
    decorator.add(mockParameters());
    verify(jsonValidator).validate(JSON, SCHEMA);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testAddStream() {
    decorator.add(Stream.of(mockParameters(), mockParameters()));

    verify(delegateRepo).add(streamCaptor.capture());
    streamCaptor
        .getValue()
        .forEach(
            t -> { // consume stream
            });
    verify(jsonValidator, times(2)).validate(JSON, SCHEMA);
  }

  @Test
  public void testNoValidationWhenNullParameters() {
    DecoratorParameters parameters = mock(DecoratorParameters.class, RETURNS_DEEP_STUBS);

    decorator.add(parameters);

    verifyZeroInteractions(jsonValidator);
  }

  private DecoratorParameters mockParameters() {
    DecoratorParameters parameters = mock(DecoratorParameters.class, RETURNS_DEEP_STUBS);
    when(parameters.getParameters()).thenReturn(JSON);
    when(parameters.getDecorator().getSchema()).thenReturn(SCHEMA);
    return parameters;
  }
}
