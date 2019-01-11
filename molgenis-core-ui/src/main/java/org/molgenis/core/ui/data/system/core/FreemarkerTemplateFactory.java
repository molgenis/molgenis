package org.molgenis.core.ui.data.system.core;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class FreemarkerTemplateFactory
    extends AbstractSystemEntityFactory<FreemarkerTemplate, FreemarkerTemplateMetadata, String> {
  FreemarkerTemplateFactory(
      FreemarkerTemplateMetadata freemarkerTemplate, EntityPopulator entityPopulator) {
    super(FreemarkerTemplate.class, freemarkerTemplate, entityPopulator);
  }
}
