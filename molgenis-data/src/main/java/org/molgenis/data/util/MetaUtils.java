package org.molgenis.data.util;

import static org.molgenis.data.meta.model.EntityTypeMetadata.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityTypeMetadata.BACKEND;
import static org.molgenis.data.meta.model.EntityTypeMetadata.DESCRIPTION;
import static org.molgenis.data.meta.model.EntityTypeMetadata.EXTENDS;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ID;
import static org.molgenis.data.meta.model.EntityTypeMetadata.IS_ABSTRACT;
import static org.molgenis.data.meta.model.EntityTypeMetadata.LABEL;
import static org.molgenis.data.meta.model.EntityTypeMetadata.PACKAGE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.TAGS;

import org.molgenis.data.Fetch;

public class MetaUtils {
  private MetaUtils() {}

  public static Fetch getEntityTypeFetch() {
    // TODO simplify fetch creation (in this case *all* attributes and expand xref/mrefs)
    return new Fetch()
        .field(ID)
        .field(PACKAGE)
        .field(LABEL)
        .field(DESCRIPTION)
        .field(ATTRIBUTES)
        .field(IS_ABSTRACT)
        .field(EXTENDS)
        .field(TAGS)
        .field(BACKEND);
  }
}
