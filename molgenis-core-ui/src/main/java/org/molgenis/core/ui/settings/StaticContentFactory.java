package org.molgenis.core.ui.settings;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class StaticContentFactory
    extends AbstractSystemEntityFactory<StaticContent, StaticContentMetadata, String> {
  public StaticContentFactory(
      StaticContentMetadata staticContentMetadata, EntityPopulator entityPopulator) {
    super(StaticContent.class, staticContentMetadata, entityPopulator);
  }
}
