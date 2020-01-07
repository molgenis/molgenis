package org.molgenis.data.validation;

import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

@Component
public class FetchValidatorImpl implements FetchValidator {

  @Override
  public Fetch validateFetch(Fetch fetch, EntityType entityType) {
    if (fetch.isValidated()) {
      return fetch;
    }

    Fetch validatedFetch = new Fetch(true);
    Attribute idAttribute = entityType.getIdAttribute();
    if (idAttribute != null && !fetch.hasField(idAttribute)) {
      validatedFetch.field(idAttribute.getName());
    }
    fetch
        .getFields()
        .forEach(
            field -> {
              Fetch subFetch = fetch.getFetch(field);
              if (subFetch != null) {
                Fetch validatedSubFetch =
                    validateFetch(subFetch, entityType.getAttribute(field).getRefEntity());
                validatedFetch.field(field, validatedSubFetch);
              } else {
                validatedFetch.field(field);
              }
            });
    return validatedFetch;
  }
}
