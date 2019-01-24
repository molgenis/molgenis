package org.molgenis.data.decorator;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.meta.DecoratorParameters;
import org.molgenis.validation.JsonValidator;

/**
 * Triggers JSON validation for the 'parameters' field of a {@link DecoratorParameters) entity.
 */
public class DecoratorParametersRepositoryDecorator
    extends AbstractRepositoryDecorator<DecoratorParameters> {
  private final JsonValidator jsonValidator;

  DecoratorParametersRepositoryDecorator(
      Repository<DecoratorParameters> delegateRepository, JsonValidator jsonValidator) {
    super(delegateRepository);
    this.jsonValidator = jsonValidator;
  }

  @Override
  public void update(DecoratorParameters decoratorParameters) {
    validateParameters(decoratorParameters);
    delegate().update(decoratorParameters);
  }

  @Override
  public void update(Stream<DecoratorParameters> decoratorParameters) {
    delegate()
        .update(
            decoratorParameters.filter(
                parameters -> {
                  validateParameters(parameters);
                  return true;
                }));
  }

  @Override
  public void add(DecoratorParameters decoratorParameters) {
    validateParameters(decoratorParameters);
    delegate().add(decoratorParameters);
  }

  @Override
  public Integer add(Stream<DecoratorParameters> decoratorParameters) {
    return delegate()
        .add(
            decoratorParameters.filter(
                parameters -> {
                  validateParameters(parameters);
                  return true;
                }));
  }

  private void validateParameters(DecoratorParameters decoratorParameters) {
    String schema = decoratorParameters.getDecorator().getSchema();
    String parameters = decoratorParameters.getParameters();
    if (schema != null && parameters != null && !parameters.isEmpty()) {
      jsonValidator.validate(parameters, schema);
    }
  }
}
