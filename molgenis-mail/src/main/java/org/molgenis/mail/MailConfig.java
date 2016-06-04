package org.molgenis.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;
import java.util.function.Supplier;

import static java.lang.String.valueOf;

@ComponentScan(basePackages = { "org.molgenis.mail", "org.molgenis.data.settings" })
@Configuration
public class MailConfig
{
	@Autowired
	private MailSettings mailSettings;

	@Bean
	public Supplier<MailSender> mailSenderSupplier()
	{
		return this::createMailSender;
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
		Properties javaMailProperties = new Properties();
		javaMailProperties.setProperty("mail.smtp.auth", valueOf(mailSettings.isAuth()));
		javaMailProperties.setProperty("mail.smtp.starttls.enable", valueOf(mailSettings.isStartTlsEnable()));
		javaMailProperties.setProperty("mail.smtp.quitwait", valueOf(mailSettings.isQuitWait()));
		mailSender.setJavaMailProperties(javaMailProperties);
		return mailSender;
	}
}