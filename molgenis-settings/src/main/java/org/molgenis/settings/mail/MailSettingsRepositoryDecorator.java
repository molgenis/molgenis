package org.molgenis.settings.mail;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.util.mail.MailSenderFactory;
import org.molgenis.util.mail.MailSettings;

import static java.util.Objects.requireNonNull;

public class MailSettingsRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final Repository<Entity> decoratedRepository;
	private final MailSenderFactory mailSenderFactory;

	public MailSettingsRepositoryDecorator(Repository<Entity> decoratedRepository, MailSenderFactory mailSenderFactory)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.mailSenderFactory = requireNonNull(mailSenderFactory);
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepository;
	}

	@Override
	public void add(Entity entity)
	{
		validate(new MailSettingsImpl(entity));
		delegate().add(entity);
	}

	@Override
	public void update(Entity entity)
	{
		validate(new MailSettingsImpl(entity));
		delegate().update(entity);
	}

	/**
	 * Validates MailSettings.
	 *
	 * @param mailSettings the MailSettings to validate
	 */
	private void validate(MailSettings mailSettings)
	{
		if (mailSettings.isTestConnection() && mailSettings.getUsername() != null && mailSettings.getPassword() != null)
		{
			mailSenderFactory.validateConnection(mailSettings);
		}
	}
}
