package org.molgenis.script.core;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class ScriptTypeFactory
    extends AbstractSystemEntityFactory<ScriptType, ScriptTypeMetadata, String> {

  ScriptTypeFactory(ScriptTypeMetadata scriptTypeMetaData, EntityPopulator entityPopulator) {
    super(ScriptType.class, scriptTypeMetaData, entityPopulator);
  }
}
