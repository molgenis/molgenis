package org.molgenis.data.validation.meta;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.validation.MolgenisValidationException;

class TagValidatorTest {
  private TagValidator tagValidator;

  @BeforeEach
  void setUpBeforeMethod() {
    tagValidator = new TagValidator();
  }

  @Test
  void validateValid() {
    Tag tag = mock(Tag.class);
    when(tag.getRelationIri()).thenReturn(Relation.isRealizationOf.getIRI());
    tagValidator.validate(tag);
  }

  @SuppressWarnings("deprecation")
  @Test
  void validateInvalid() {
    Tag tag = mock(Tag.class);
    when(tag.getRelationIri()).thenReturn("blaat");
    assertThrows(MolgenisValidationException.class, () -> tagValidator.validate(tag));
  }
}
