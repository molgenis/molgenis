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
    NameValidator.validateEntityName("6valid");
  }

  @Test
  void testValidateAttributeNameStartsWithDigit() {
    assertThrows(
        MolgenisDataException.class, () -> NameValidator.validateAttributeName("6invalid"));
  }

  @Test
  void testReservedKeyword() {
    assertThrows(MolgenisDataException.class, () -> NameValidator.validateEntityName("base"));
  }

  @Test
  void testI18nAttributeNameMulti() {
    assertThrows(
        MolgenisDataException.class, () -> NameValidator.validateAttributeName("test-en-nl"));
  }

  @Test
  void testI18nAttributeNameTooLong() {
    assertThrows(
        MolgenisDataException.class, () -> NameValidator.validateAttributeName("test-xxxx"));
  }

  @Test
  void testI18nAttributeNameMissing() {
    assertThrows(MolgenisDataException.class, () -> NameValidator.validateAttributeName("test-"));
  }

  @Test
  void testI18nAttributeNameUpperCase() {
    assertThrows(MolgenisDataException.class, () -> NameValidator.validateAttributeName("test-NL"));
  }

  @Test
  void testI18nAttributeNameNumber() {
    assertThrows(MolgenisDataException.class, () -> NameValidator.validateAttributeName("test-n2"));
  }

  @Test
  void testI18nNameMulti() {
    NameValidator.validateEntityName("test-en-nl");
  }

  @Test
  void testI18nTooLong() {
    NameValidator.validateEntityName("test-xxxx");
  }

  @Test
  void testI18nMissing() {
    NameValidator.validateEntityName("test-");
  }

  @Test
  void testI18nUpperCase() {
    NameValidator.validateEntityName("test-NL");
  }

  @Test
  void testI18nNumber() {
    NameValidator.validateEntityName("test-n2");
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
