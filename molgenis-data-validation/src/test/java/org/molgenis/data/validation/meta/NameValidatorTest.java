package org.molgenis.data.validation.meta;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.molgenis.data.MolgenisDataException;

class NameValidatorTest {
  @Test
  void testValidateNameInvalidCharacters() {
    assertThrows(
        MolgenisDataException.class, () -> NameValidator.validateEntityName("Invalid.Name"));
  }

  @Test
  void testValidateNameStartsWithDigit() {
    assertThrows(MolgenisDataException.class, () -> NameValidator.validateEntityName("6invalid"));
  }

  @Test
  void testReservedKeyword() {
    assertThrows(MolgenisDataException.class, () -> NameValidator.validateEntityName("base"));
  }

  @Test
  void testI18nNameMilti() {
    assertThrows(MolgenisDataException.class, () -> NameValidator.validateEntityName("test-en-nl"));
  }

  @Test
  void testI18nTooLong() {
    assertThrows(MolgenisDataException.class, () -> NameValidator.validateEntityName("test-xxxx"));
  }

  @Test
  void testI18nMissing() {
    assertThrows(MolgenisDataException.class, () -> NameValidator.validateEntityName("test-"));
  }

  @Test
  void testI18nUpperCase() {
    assertThrows(MolgenisDataException.class, () -> NameValidator.validateEntityName("test-NL"));
  }

  @Test
  void testI18nNumber() {
    assertThrows(MolgenisDataException.class, () -> NameValidator.validateEntityName("test-n2"));
  }

  @Test
  void testI18nName() {
    NameValidator.validateAttributeName("test-en");
    NameValidator.validateAttributeName("test-eng");
  }

  @Test
  void testUnderscoreAttr() {
    NameValidator.validateAttributeName("test_test");
  }
}
