package org.molgenis.data.decorator;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.meta.DecoratorParameters;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.validation.JsonValidator;

class DecoratorParametersRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private JsonValidator jsonValidator;

  @Mock private Repository delegateRepo;

  @Captor private ArgumentCaptor<Stream<DecoratorParameters>> streamCaptor;

  private DecoratorParametersRepositoryDecorator decorator;

  private static final String SCHEMA = "{some: 'schema'}";
  private static final String JSON = "{para: 'meters'}";

  @BeforeEach
  @SuppressWarnings("unchecked")
  void beforeMethod() {
    decorator = new DecoratorParametersRepositoryDecorator(delegateRepo, jsonValidator);
  }

  @Test
  void testUpdate() {
    decorator.update(mockParameters());
    verify(jsonValidator).validate(JSON, SCHEMA);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testUpdateStream() {
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
  void testAdd() {
    decorator.add(mockParameters());
    verify(jsonValidator).validate(JSON, SCHEMA);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testAddStream() {
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
  void testNoValidationWhenNullParameters() {
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
