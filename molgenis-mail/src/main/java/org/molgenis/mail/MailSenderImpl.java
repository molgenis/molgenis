package org.molgenis.mail;

import org.molgenis.data.DataService;
import org.molgenis.security.core.runas.RunAsSystemProxy;
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
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
public class MailSenderImpl implements MailSender
{
	private static final Logger LOG = LoggerFactory.getLogger(MailSenderImpl.class);

	private final DataService dataService;
	private final MailSettings mailSettings;
	private Properties defaultProperties;

	@Autowired
	public MailSenderImpl(DataService dataService, MailSettings mailSettings) throws IOException
	{
		defaultProperties = new Properties();
		defaultProperties.load(getClass().getResourceAsStream("mail-default.properties"));
		this.dataService = requireNonNull(dataService);
		this.mailSettings = requireNonNull(mailSettings);
	}

	//	@PostConstruct
	public void validateConnection()
	{
		if (mailSettings.isTestConnection())
		{
			try
			{
				JavaMailSenderImpl sender = createMailSender();
				sender.testConnection();

			}
			catch (MessagingException ex)
			{
				throw new IllegalStateException(String.format("Unable to ping to %s", this.mailSettings.getHost()), ex);
			}
		}
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
		mailSender.setJavaMailProperties(getProperties());
		return mailSender;
	}

	private Properties getProperties()
	{
		Properties result = new Properties(defaultProperties);
		RunAsSystemProxy.runAsSystem(() ->
		{
			result.putAll(dataService.findAll(MailSenderPropertyType.MAIL_SENDER_PROPERTY, MailSenderProperty.class)
					.collect(Collectors.toMap(MailSenderProperty::getKey, MailSenderProperty::getValue)));
		});
		LOG.debug("Mail properties: {}", result);
		return result;
	}
}
