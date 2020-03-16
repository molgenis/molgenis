package org.molgenis.data.validation.meta;

import static org.molgenis.data.meta.SystemEntityType.UNIFIED_IDENTIFIER_REGEX;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.molgenis.data.MolgenisDataException;

/** Validates if metadata is internally consistent and correct. */
public class NameValidator {
  // some words are reserved for the RestAPI and default packages/entities/attributes, etc.
  @SuppressWarnings("java:S2386") // false positive: Mutable fields should not be "public static"
  public static final Set<String> KEYWORDS =
      ImmutableSet.of("login", "logout", "csv", "base", "exist", "meta", "_idValue");

  private NameValidator() {}

  /** Checks if a name is a reserved keyword. */
  private static void checkForKeyword(String name) {
    if (KEYWORDS.contains(name) || KEYWORDS.contains(name.toUpperCase())) {
      throw new MolgenisDataException(
          "Name [" + name + "] is not allowed because it is a reserved keyword.");
    }
  }

  /**
   * Validates names of entities, packages and attributes. Rules: only [a-zA-Z0-9_#] are allowed,
   * name must start with a letter
   */
  public static void validateAttributeName(String name) {
    checkForKeyword(name);

    checkForLeadingDigit(name);

    if (!name.matches("[a-zA-Z0-9_#]+(-[a-z]{2,3})??$")) {
      throw new MolgenisDataException(
          "Invalid characters in: ["
              + name
              + "] Only letters (a-z, A-Z), digits (0-9), underscores (_) and hashes (#) are allowed.");
    }
  }

  public static void validateEntityName(String name) {
    checkForKeyword(name);
    checkForIllegalCharacters(name);
  }

  private static void checkForLeadingDigit(String name) {
    if (Character.isDigit(name.charAt(0))) {
      throw new MolgenisDataException(
          "Invalid name: [" + name + "] Names must start with a letter.");
    }
  }

  private static void checkForIllegalCharacters(String name) {
    if (!name.matches(UNIFIED_IDENTIFIER_REGEX)) {
      throw new MolgenisDataException(
          "Invalid characters in: ["
              + name
              + "] Only letters (a-z, A-Z), digits (0-9), underscores(_) and dashes(-) are allowed.");
    }
  }
}
