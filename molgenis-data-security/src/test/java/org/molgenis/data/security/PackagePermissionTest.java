package org.molgenis.data.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.util.i18n.AllPropertiesMessageSource;
import org.molgenis.util.i18n.MessageSourceHolder;
import org.molgenis.util.i18n.TestAllPropertiesMessageSource;
import org.molgenis.util.i18n.format.MessageFormatFactory;

class PackagePermissionTest {
  private PackagePermission addPackagePermission = PackagePermission.ADD_PACKAGE;
  private MessageFormatFactory messageFormatFactory = new MessageFormatFactory();
  private AllPropertiesMessageSource messageSource;

  @BeforeEach
  void exceptionMessageTestBeforeMethod() {
    messageSource = new TestAllPropertiesMessageSource(messageFormatFactory);
    messageSource.addMolgenisNamespaces("data-security");
    MessageSourceHolder.setMessageSource(messageSource);
  }

  @Test
  void testNameEnglish() {
    assertEquals(
        messageSource.getMessage(addPackagePermission.getName(), Locale.ENGLISH), "Add package");
  }

  @Test
  void testDescription() {
    assertEquals(
        messageSource.getMessage(addPackagePermission.getDescription(), Locale.ENGLISH),
        "Permission to add a child package to this package");
  }
}
