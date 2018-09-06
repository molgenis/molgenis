package org.molgenis.data.decorator.meta;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.ENTITY_TYPE_ID;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.PARAMETERS;

import java.util.stream.Stream;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class DecoratorConfiguration extends StaticEntity {
  public DecoratorConfiguration(Entity entity) {
    super(entity);
  }

  public DecoratorConfiguration(EntityType entityType) {
    super(entityType);
  }

  public String getEntityTypeId() {
    return getString(ENTITY_TYPE_ID);
  }

  public void setEntityTypeId(String entityTypeId) {
    set(ENTITY_TYPE_ID, entityTypeId);
  }

  public Stream<DecoratorParameters> getDecoratorParameters() {
    return stream(getEntities(PARAMETERS, DecoratorParameters.class).spliterator(), false);
  }

  public void setDecoratorParameters(Stream<DecoratorParameters> parameters) {
    set(PARAMETERS, parameters.collect(toList()));
  }
}
