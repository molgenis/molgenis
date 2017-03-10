package org.molgenis.settings.mail;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.util.mail.MailSenderFactory;
import org.molgenis.util.mail.MailSettings;

import static java.util.Objects.requireNonNull;

public class MailSettingsRepositoryDecorator extends AbstractRepositoryDecorator<MailSettingsImpl>
{
	private final Repository<MailSettingsImpl> decoratedRepository;
	private final MailSenderFactory mailSenderFactory;

	public MailSettingsRepositoryDecorator(Repository<MailSettingsImpl> decoratedRepository,
			MailSenderFactory mailSenderFactory)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.mailSenderFactory = requireNonNull(mailSenderFactory);
	}

	@Override
	protected Repository<MailSettingsImpl> delegate()
	{
		return decoratedRepository;
	}

	@Override
	public void add(MailSettingsImpl entity)
	{
		validate(entity);
		delegate().add(entity);
	}

	@Override
	public void update(MailSettingsImpl entity)
	{
		validate(entity);
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
