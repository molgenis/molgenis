package org.molgenis.data.decorator.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class DecoratorParametersFactory
    extends AbstractSystemEntityFactory<DecoratorParameters, DecoratorParametersMetadata, String> {
  DecoratorParametersFactory(
      DecoratorParametersMetadata decoratorParametersMetadata, EntityPopulator entityPopulator) {
    super(DecoratorParameters.class, decoratorParametersMetadata, entityPopulator);
  }
}
