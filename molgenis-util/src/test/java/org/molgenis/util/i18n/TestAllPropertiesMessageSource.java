package org.molgenis.util.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import org.molgenis.util.i18n.format.MessageFormatFactory;

public class TestAllPropertiesMessageSource extends AllPropertiesMessageSource {
  private final MessageFormatFactory messageFormatFactory;

  public TestAllPropertiesMessageSource(MessageFormatFactory messageFormatFactory) {
    this.messageFormatFactory = Objects.requireNonNull(messageFormatFactory);
  }

  @Override
  protected MessageFormat createMessageFormat(String msg, Locale locale) {
    return messageFormatFactory.createMessageFormat(msg, locale);
  }
}
