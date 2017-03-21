package org.molgenis.util.mail;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class JavaMailSenderFactoryTest extends AbstractMockitoTest
{
	private JavaMailSenderFactory javaMailSenderFactory;
	@Mock
	private MailSettings mailSettings;

	@BeforeMethod
	public void beforeMethod()
	{
		when(mailSettings.getHost()).thenReturn("host");
		when(mailSettings.getPort()).thenReturn(1234);
		when(mailSettings.getUsername()).thenReturn("username");
		when(mailSettings.getPassword()).thenReturn("password");
		when(mailSettings.getDefaultEncoding()).thenReturn(UTF_8);
		when(mailSettings.getJavaMailProperties()).thenReturn(new Properties());
		javaMailSenderFactory = new JavaMailSenderFactory();
	}

	@Test
	public void testCreateMailSenderWithDefaultProperties()
	{
		JavaMailSenderImpl actual = javaMailSenderFactory.createMailSender(mailSettings);

		assertEquals(actual.getHost(), "host");
		assertEquals(actual.getPort(), 1234);
		assertEquals(actual.getUsername(), "username");
		assertEquals(actual.getPassword(), "password");
		assertEquals(actual.getDefaultEncoding(), "UTF-8");
		final Properties actualProperties = actual.getJavaMailProperties();
		assertEquals(actualProperties.getProperty("mail.smtp.starttls.enable"), "true");
		assertEquals(actualProperties.getProperty("mail.smtp.quitwait"), "false");
		assertEquals(actualProperties.getProperty("mail.smtp.auth"), "true");
	}

	@Test
	public void testCreateMailSenderWithSpecifiedProperties()
	{
		final Properties javaMailProps = new Properties();
		javaMailProps.put("mail.debug", "true"); // specify
		javaMailProps.put("mail.smtp.starttls.enable", "false"); // override
		when(mailSettings.getJavaMailProperties()).thenReturn(javaMailProps);

		JavaMailSenderImpl actual = javaMailSenderFactory.createMailSender(mailSettings);

		assertEquals(actual.getHost(), "host");
		assertEquals(actual.getPort(), 1234);
		assertEquals(actual.getUsername(), "username");
		assertEquals(actual.getPassword(), "password");
		assertEquals(actual.getDefaultEncoding(), "UTF-8");
		final Properties actualProperties = actual.getJavaMailProperties();
		assertEquals(actualProperties.getProperty("mail.smtp.starttls.enable"), "false");
		assertEquals(actualProperties.getProperty("mail.smtp.quitwait"), "false");
		assertEquals(actualProperties.getProperty("mail.smtp.auth"), "true");
		assertEquals(actualProperties.getProperty("mail.debug"), "true");
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Unable to ping to host")
	public void testValidateConnectionInvalidConnection()
	{
		javaMailSenderFactory.validateConnection(mailSettings);
	}
}
