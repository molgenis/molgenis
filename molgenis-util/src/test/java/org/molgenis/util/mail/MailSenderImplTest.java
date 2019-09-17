package org.molgenis.util.mail;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

class MailSenderImplTest extends AbstractMockitoTest {
  private MailSenderImpl mailSender;

  @Mock private MailSettings mailSettings;
  @Mock private MailSenderFactory mailSenderFactory;
  @Mock private MailSender actualMailSender;
  @Mock private SimpleMailMessage simpleMailMessage;
  @Mock private SimpleMailMessage secondSimpleMailMessage;

  @BeforeEach
  void beforeMethod() {
    mailSender = new MailSenderImpl(mailSettings, mailSenderFactory);
  }

  @Test
  void testSendSingleMessage() {
    when(mailSenderFactory.createMailSender(mailSettings)).thenReturn(actualMailSender);
    mailSender.send(simpleMailMessage);
    verify(actualMailSender).send(simpleMailMessage);
  }

  @Test
  void testSendTwoMessages() {
    when(mailSenderFactory.createMailSender(mailSettings)).thenReturn(actualMailSender);
    mailSender.send(simpleMailMessage, secondSimpleMailMessage);
    verify(actualMailSender).send(simpleMailMessage, secondSimpleMailMessage);
  }
}
