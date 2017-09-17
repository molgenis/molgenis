package org.molgenis.util.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class MailSenderImpl implements MailSender
{
	private static final Logger LOG = LoggerFactory.getLogger(MailSenderImpl.class);

	private final MailSettings mailSettings;
	private final MailSenderFactory mailSenderFactory;

	public MailSenderImpl(MailSettings mailSettings, MailSenderFactory mailSenderFactory) 
	{
		this.mailSettings = requireNonNull(mailSettings);
		this.mailSenderFactory = requireNonNull(mailSenderFactory);
	}

	@Override
	public void send(SimpleMailMessage simpleMessage)
	{
		LOG.trace("Sending message...");
		createMailSender().send(simpleMessage);
		LOG.debug("Sent message.");
	}

	@Override
	public void send(SimpleMailMessage... simpleMessages)
	{
		LOG.trace("Sending messages...");
		createMailSender().send(simpleMessages);
		LOG.debug("Sent messages.");
	}

	private MailSender createMailSender()
	{
		return mailSenderFactory.createMailSender(mailSettings);
	}
}