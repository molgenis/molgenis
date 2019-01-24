package org.molgenis.data.i18n.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class L10nStringFactory
    extends AbstractSystemEntityFactory<L10nString, L10nStringMetadata, String> {
  L10nStringFactory(L10nStringMetadata l10NStringMetadata, EntityPopulator entityPopulator) {
    super(L10nString.class, l10NStringMetadata, entityPopulator);
  }
}
