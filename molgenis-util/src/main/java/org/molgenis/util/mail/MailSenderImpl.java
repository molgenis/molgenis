package org.molgenis.util.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Properties;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Component
public class MailSenderImpl implements MailSender
{
	private static final Logger LOG = LoggerFactory.getLogger(MailSenderImpl.class);

	private static Properties defaultProperties = new Properties();

	static
	{
		try
		{
			defaultProperties.load(MailSenderImpl.class.getResourceAsStream("mail-default.properties"));
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Default mail-default.properties not found!");
		}
	}

	private final MailSettings mailSettings;

	@Autowired
	public MailSenderImpl(MailSettings mailSettings) throws IOException
	{
		this.mailSettings = requireNonNull(mailSettings);
	}

	@Override
	public void send(SimpleMailMessage simpleMessage) throws MailException
	{
		LOG.trace("Sending message...");
		createMailSender().send(simpleMessage);
		LOG.debug("Sent message.");
	}

	@Override
	public void send(SimpleMailMessage... simpleMessages) throws MailException
	{
		LOG.trace("Sending messages...");
		createMailSender().send(simpleMessages);
		LOG.debug("Sent messages.");
	}

	private JavaMailSenderImpl createMailSender()
	{
		return createMailSender(mailSettings);
	}

	private static JavaMailSenderImpl createMailSender(MailSettings mailSettings)
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
		LOG.debug("Mail properties: {}", properties);
		mailSender.setJavaMailProperties(properties);
		return mailSender;
	}

	public static void validateConnection(MailSettings mailSettings)
	{
		if (mailSettings.isTestConnection() && mailSettings.getUsername() != null && mailSettings.getPassword() != null)
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
}