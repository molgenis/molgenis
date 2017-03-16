package org.molgenis.util.mail;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MailSenderImplTest extends AbstractMockitoTest
{
	private MailSenderImpl mailSender;

	@Mock
	MailSettings mailSettings;
	@Mock
	MailSenderFactory mailSenderFactory;
	@Mock
	MailSender actualMailSender;
	@Mock
	SimpleMailMessage simpleMailMessage;
	@Mock
	SimpleMailMessage secondSimpleMailMessage;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		mailSender = new MailSenderImpl(mailSettings, mailSenderFactory);
	}

	@Test
	public void testSendSingleMessage()
	{
		when(mailSenderFactory.createMailSender(mailSettings)).thenReturn(actualMailSender);
		mailSender.send(simpleMailMessage);
		verify(actualMailSender).send(simpleMailMessage);
	}

	@Test
	public void testSendTwoMessages()
	{
		when(mailSenderFactory.createMailSender(mailSettings)).thenReturn(actualMailSender);
		mailSender.send(simpleMailMessage, secondSimpleMailMessage);
		verify(actualMailSender).send(simpleMailMessage, secondSimpleMailMessage);
	}
}
