package org.molgenis.util.mail;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.molgenis.util.mail.JavaMailSenderFactory.MAIL_SMTP_AUTH;
import static org.molgenis.util.mail.JavaMailSenderFactory.MAIL_SMTP_FROM_ADDRESS;
import static org.molgenis.util.mail.JavaMailSenderFactory.MAIL_SMTP_QUITWAIT;
import static org.molgenis.util.mail.JavaMailSenderFactory.MAIL_SMTP_STARTTLS_ENABLE;

import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class JavaMailSenderFactoryTest extends AbstractMockitoTest {
  private JavaMailSenderFactory javaMailSenderFactory;
  @Mock private MailSettings mailSettings;

  @BeforeEach
  void beforeMethod() {
    when(mailSettings.getHost()).thenReturn("host");
    when(mailSettings.getPort()).thenReturn(1234);
    when(mailSettings.getUsername()).thenReturn("username");
    when(mailSettings.getPassword()).thenReturn("password");
    when(mailSettings.getDefaultEncoding()).thenReturn(UTF_8);
    when(mailSettings.isStartTlsEnabled()).thenReturn("true");
    when(mailSettings.isQuitWait()).thenReturn("false");
    when(mailSettings.isAuthenticationRequired()).thenReturn("true");
    when(mailSettings.getFromAddress()).thenReturn("molgenis@gmail.com");
    when(mailSettings.getJavaMailProperties()).thenReturn(new Properties());
    javaMailSenderFactory = new JavaMailSenderFactory();
  }

  @Test
  void testCreateMailSenderWithDefaultProperties() {
    JavaMailSenderImpl actual = javaMailSenderFactory.createMailSender(mailSettings);

    assertEquals("host", actual.getHost());
    assertEquals(1234, actual.getPort());
    assertEquals("username", actual.getUsername());
    assertEquals("password", actual.getPassword());
    assertEquals("UTF-8", actual.getDefaultEncoding());
    final Properties actualProperties = actual.getJavaMailProperties();
    assertEquals("true", actualProperties.getProperty(MAIL_SMTP_STARTTLS_ENABLE));
    assertEquals("false", actualProperties.getProperty(MAIL_SMTP_QUITWAIT));
    assertEquals("true", actualProperties.getProperty(MAIL_SMTP_AUTH));
    assertEquals("molgenis@gmail.com", actualProperties.getProperty(MAIL_SMTP_FROM_ADDRESS));
  }

  // regression test for https://github.com/molgenis/molgenis/issues/6516
  @Test
  void testCreateMailSenderWithoutUsernamePassword() {
    JavaMailSenderImpl actual = javaMailSenderFactory.createMailSender(mailSettings);

    assertEquals("host", actual.getHost());
    assertEquals(1234, actual.getPort());
    assertEquals("UTF-8", actual.getDefaultEncoding());
    final Properties actualProperties = actual.getJavaMailProperties();
    assertEquals("true", actualProperties.getProperty("mail.smtp.starttls.enable"));
    assertEquals("false", actualProperties.getProperty("mail.smtp.quitwait"));
    assertEquals("true", actualProperties.getProperty("mail.smtp.auth"));
  }

  @Test
  void testCreateMailSenderWithSpecifiedProperties() {
    final Properties javaMailProps = new Properties();
    javaMailProps.put("mail.debug", "true"); // specify
    javaMailProps.put("mail.smtp.starttls.enable", "false"); // override
    when(mailSettings.getJavaMailProperties()).thenReturn(javaMailProps);

    JavaMailSenderImpl actual = javaMailSenderFactory.createMailSender(mailSettings);

    assertEquals("host", actual.getHost());
    assertEquals(1234, actual.getPort());
    assertEquals("username", actual.getUsername());
    assertEquals("password", actual.getPassword());
    assertEquals("UTF-8", actual.getDefaultEncoding());
    final Properties actualProperties = actual.getJavaMailProperties();
    assertEquals("false", actualProperties.getProperty("mail.smtp.starttls.enable"));
    assertEquals("false", actualProperties.getProperty("mail.smtp.quitwait"));
    assertEquals("true", actualProperties.getProperty("mail.smtp.auth"));
    assertEquals("true", actualProperties.getProperty("mail.debug"));
  }

  @Test
  void testValidateConnectionInvalidConnection() {
    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> javaMailSenderFactory.validateConnection(mailSettings));
    assertEquals("Unable to ping to host", exception.getMessage());
  }
}
