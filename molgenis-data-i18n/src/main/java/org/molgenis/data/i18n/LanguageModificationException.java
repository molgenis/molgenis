package org.molgenis.data.i18n;

public class LanguageModificationException extends RuntimeException {

  @Override
  public String getMessage() {
    return "Adding and Deleting of languages is not allowed";
  }
}
