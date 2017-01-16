package org.molgenis.settings.mail;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.util.mail.MailSenderImpl;

import static java.util.Objects.requireNonNull;

public class MailSettingsRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final Repository<Entity> decoratedRepository;

	public MailSettingsRepositoryDecorator(Repository<Entity> decoratedRepository)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepository;
	}

	@Override
	public void add(Entity entity)
	{
		MailSenderImpl.validateConnection(new MailSettingsImpl(entity));
		delegate().add(entity);
	}

	@Override
	public void update(Entity entity)
	{
		MailSenderImpl.validateConnection(new MailSettingsImpl(entity));
		delegate().update(entity);
	}
}
