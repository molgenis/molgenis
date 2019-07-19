package org.molgenis.api.data.v3;

import static org.molgenis.api.model.Selection.EMPTY_SELECTION;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.Selection;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.EntityTypeUtils;

public class FetchMapper {
  public @CheckForNull @Nullable Fetch toFetch(
      EntityType entityType, Selection filter, Selection expand) {
    if (!filter.hasItems()) {
      return null;
    }

    Fetch fetch = new Fetch();

    Iterable<Attribute> attributes = entityType.getAtomicAttributes();
    attributes.forEach(
        attribute -> {
          String attributeName = attribute.getName();

          if (filter.hasItem(attributeName)) {
            Fetch subFetch;
            if (expand.hasItem(attributeName) && EntityTypeUtils.isReferenceType(attribute)) {
              Selection subFilter = filter.getSelection(attributeName).orElse(EMPTY_SELECTION);
              Selection subExpand = expand.getSelection(attributeName).orElse(EMPTY_SELECTION);
              subFetch = toFetch(attribute.getRefEntity(), subFilter, subExpand);
            } else {
              subFetch = null;
            }

            fetch.field(attributeName, subFetch);
          }
        });

    return fetch;
  }
}
