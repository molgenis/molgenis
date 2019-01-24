package org.molgenis.data.decorator;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.meta.DecoratorParameters;
import org.molgenis.data.decorator.meta.DecoratorParametersMetadata;
import org.molgenis.validation.JsonValidator;
import org.springframework.stereotype.Component;

@Component
public class DecoratorParametersRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<
        DecoratorParameters, DecoratorParametersMetadata> {
  private final JsonValidator jsonValidator;

  public DecoratorParametersRepositoryDecoratorFactory(
      DecoratorParametersMetadata decoratorParametersMetadata, JsonValidator jsonValidator) {
    super(decoratorParametersMetadata);
    this.jsonValidator = requireNonNull(jsonValidator);
  }

  @Override
  public Repository<DecoratorParameters> createDecoratedRepository(
      Repository<DecoratorParameters> repository) {
    return new DecoratorParametersRepositoryDecorator(repository, jsonValidator);
  }
}
