package org.molgenis.data.validation;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.IndexedRepository;

public class IndexedRepositoryValidationDecorator extends RepositoryValidationDecorator implements IndexedRepository
{
	private final IndexedRepository decoratedRepository;

	public IndexedRepositoryValidationDecorator(DataService dataService, IndexedRepository decoratedRepository,
			EntityAttributesValidator entityAttributesValidator)
	{
		super(dataService, decoratedRepository, entityAttributesValidator);
		this.decoratedRepository = decoratedRepository;
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepository.rebuildIndex();
	}

	@Override
	public void create()
	{
		decoratedRepository.create();
	}

	@Override
	public void drop()
	{
		decoratedRepository.drop();
	}
}
