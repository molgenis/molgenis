package org.molgenis.settings.mail;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.util.mail.MailSenderFactory;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class MailSettingsRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<Entity, MailSettingsImpl.Meta>
{
	private final MailSenderFactory mailSenderFactory;

	public MailSettingsRepositoryDecoratorFactory(MailSettingsImpl.Meta mailSettingsMeta,
			MailSenderFactory mailSenderFactory)
	{
		super(mailSettingsMeta);
		this.mailSenderFactory = requireNonNull(mailSenderFactory);
	}

	@Override
	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		return new MailSettingsRepositoryDecorator(repository, mailSenderFactory);
	}
}
