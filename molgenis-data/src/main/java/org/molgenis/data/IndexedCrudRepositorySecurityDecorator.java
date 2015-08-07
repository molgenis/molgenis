package org.molgenis.data;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.AggregateAnonymizerImpl;
import org.molgenis.security.core.Permission;

public class IndexedCrudRepositorySecurityDecorator extends RepositorySecurityDecorator implements IndexedRepository
{
	private final IndexedRepository decoratedRepository;
	private final AppSettings appSettings;
	private final AggregateAnonymizer aggregateAnonymizer = new AggregateAnonymizerImpl();

	public IndexedCrudRepositorySecurityDecorator(IndexedRepository decoratedRepository, AppSettings appSettings)
	{
		super(decoratedRepository);
		this.decoratedRepository = checkNotNull(decoratedRepository);
		this.appSettings = checkNotNull(appSettings);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);

		Integer threshold = appSettings.getAggregateThreshold();

		AggregateResult result = decoratedRepository.aggregate(aggregateQuery);
		if (threshold != null && threshold > 0)
		{
			result = aggregateAnonymizer.anonymize(result, threshold);
		}
		return result;
	}

	@Override
	public void rebuildIndex()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.rebuildIndex();
	}

	@Override
	public void create()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.create();
	}

	@Override
	public void drop()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.drop();
	}
}
