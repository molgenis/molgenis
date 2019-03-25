package org.molgenis.util.mail;

import java.nio.charset.Charset;
import java.util.Properties;

public interface MailSettings {
  String getHost();

  int getPort();

  String getProtocol();

  String getUsername();

  String getPassword();

  Charset getDefaultEncoding();

  boolean isTestConnection();

  String isStartTlsEnabled();

  String isQuitWait();

  String isAuthenticationRequired();

  String getFromAddress();

  Properties getJavaMailProperties();


}
