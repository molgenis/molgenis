package org.molgenis.data.importer;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class ImportRunFactory
    extends AbstractSystemEntityFactory<ImportRun, ImportRunMetadata, String> {
  ImportRunFactory(ImportRunMetadata importRunMetadata, EntityPopulator entityPopulator) {
    super(ImportRun.class, importRunMetadata, entityPopulator);
  }
}
