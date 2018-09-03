package org.molgenis.data.validation.meta;

import static java.lang.String.format;

import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.validation.ConstraintViolation;
import org.springframework.stereotype.Component;

/** {@link org.molgenis.data.meta.model.Tag Tag} validator */
@Component
public class TagValidator {
  /**
   * Validates tag
   *
   * @param tag tag
   * @throws MolgenisValidationException if tag is not valid
   */
  @SuppressWarnings("MethodMayBeStatic")
  public void validate(Tag tag) {
    String relationIri = tag.getRelationIri();
    Relation relation = Relation.forIRI(relationIri);
    if (relation == null) {
      throw new MolgenisValidationException(
          new ConstraintViolation(format("Unknown relation IRI [%s]", relationIri)));
    }
  }
}
