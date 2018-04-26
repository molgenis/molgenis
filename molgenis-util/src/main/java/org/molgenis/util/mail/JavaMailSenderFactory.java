package org.molgenis.util.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Properties;

import static java.lang.String.format;

@Component
public class JavaMailSenderFactory implements MailSenderFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(JavaMailSenderFactory.class);
	private static Properties defaultProperties = new Properties();

	static
	{
		try
		{
			defaultProperties.load(JavaMailSenderFactory.class.getResourceAsStream("mail-default.properties"));
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Default mail-default.properties not found!");
		}
	}

	@Override
	public JavaMailSenderImpl createMailSender(MailSettings mailSettings)
	{
		LOG.trace("createMailSender");
		if (mailSettings.getUsername() == null || mailSettings.getPassword() == null)
		{
			throw new IllegalStateException(
					"Cannot create mail sender. Username or password of the mail account not specified in the system mail settings.");
		}

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(mailSettings.getHost());
		mailSender.setPort(mailSettings.getPort());
		mailSender.setProtocol(mailSettings.getProtocol());
		mailSender.setUsername(mailSettings.getUsername());
		mailSender.setPassword(mailSettings.getPassword());
		mailSender.setDefaultEncoding(mailSettings.getDefaultEncoding().name());
		Properties properties = new Properties(defaultProperties);
		properties.putAll(mailSettings.getJavaMailProperties());
		LOG.debug("Mail properties: {}; defaults: {}", properties, defaultProperties);
		mailSender.setJavaMailProperties(properties);
		return mailSender;
	}

	@Override
	public void validateConnection(MailSettings mailSettings)
	{
		LOG.info("Validating mail settings...");
		try
		{
			JavaMailSenderImpl sender = createMailSender(mailSettings);
			sender.testConnection();
			LOG.info("OK.");
		}
		catch (MessagingException ex)
		{
			String message = format("Unable to ping to %s", mailSettings.getHost());
			LOG.info(message, ex);
			throw new IllegalStateException(message, ex);
		}

	}
}
