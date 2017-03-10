package org.molgenis.settings.mail;

import org.molgenis.data.RepositoryDecoratorRegistry;
import org.molgenis.util.mail.MailSenderFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static java.util.Objects.requireNonNull;
import static org.molgenis.settings.mail.MailSettingsImpl.Meta.MAIL_SETTINGS;

@Configuration
public class MailSettingsConfig
{
	private final RepositoryDecoratorRegistry repositoryDecoratorRegistry;
	private final MailSenderFactory mailSenderFactory;

	public MailSettingsConfig(RepositoryDecoratorRegistry repositoryDecoratorRegistry,
			MailSenderFactory mailSenderFactory)
	{
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
		this.mailSenderFactory = requireNonNull(mailSenderFactory);
	}

	@PostConstruct
	public void init()
	{
		repositoryDecoratorRegistry.addFactory(MAIL_SETTINGS,
				repository -> new MailSettingsRepositoryDecorator(repository, mailSenderFactory));
	}
}
