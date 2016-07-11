package org.molgenis.data.idcard;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.Entity;
import org.molgenis.data.idcard.indexer.IdCardIndexerService;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.molgenis.data.settings.SettingsEntityListener;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdCardBootstrapper
{
	private final IdCardIndexerService idCardIndexerService;
	private final IdCardIndexerSettings idCardIndexerSettings;

	@Autowired
	public IdCardBootstrapper(IdCardIndexerService idCardIndexerService, IdCardIndexerSettings idCardIndexerSettings)
	{

		this.idCardIndexerService = requireNonNull(idCardIndexerService);
		this.idCardIndexerSettings = requireNonNull(idCardIndexerSettings);
	}

	public void bootstrap()
	{
		idCardIndexerSettings.addListener(new SettingsEntityListener()
		{
			@Override
			public void postUpdate(Entity entity)
			{
				try
				{
					idCardIndexerService.updateIndexerScheduler(false);
				}
				catch (SchedulerException e)
				{
					throw new RuntimeException(e);
				}
			}
		});

		try
		{
			idCardIndexerService.updateIndexerScheduler(true);
		}
		catch (SchedulerException e)
		{
			throw new RuntimeException(e);
		}
	}
}
