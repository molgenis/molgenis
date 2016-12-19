package org.molgenis.mail;

import org.molgenis.data.DataService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
public class MailSenderImpl implements MailSender
{
	private final DataService dataService;
	private final MailSettings mailSettings;
	private Properties defaultProperties;

	@Autowired
	public MailSenderImpl(DataService dataService, MailSettings mailSettings) throws IOException
	{
		defaultProperties = new Properties();
		//		defaultProperties.load(getClass().getResourceAsStream("mail-default.properties"));
		this.dataService = requireNonNull(dataService);
		this.mailSettings = requireNonNull(mailSettings);
	}

	@Override
	public void send(SimpleMailMessage simpleMessage) throws MailException
	{
		createMailSender().send(simpleMessage);
	}

	@Override
	public void send(SimpleMailMessage... simpleMessages) throws MailException
	{
		createMailSender().send(simpleMessages);
	}

	private MailSender createMailSender()
	{
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
		mailSender.setJavaMailProperties(getProperties());
		return mailSender;
	}

	@RunAsSystem
	public Properties getProperties()
	{
		Properties result = new Properties(defaultProperties);
		result.putAll(dataService.findAll(MailSenderPropertyType.MAIL_SENDER_PROPERTY, MailSenderProperty.class)
				.collect(Collectors.toMap(MailSenderProperty::getKey, MailSenderProperty::getValue)));
		return result;
	}
}
