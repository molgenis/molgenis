package org.molgenis.util.mail;

import org.springframework.mail.MailSender;

public interface MailSenderFactory
{
	/**
	 * Creates a MailSender for specific {@link MailSettings}.
	 *
	 * @param mailSettings the MailSettings to use
	 * @return the created MailSender
	 */
	MailSender createMailSender(MailSettings mailSettings);

	/**
	 * Validates if a connection with the mail server can be made for specific {@link MailSettings}.
	 *
	 * @param mailSettings the {@link MailSettings} to validate.
	 */
	void validateConnection(MailSettings mailSettings);
}