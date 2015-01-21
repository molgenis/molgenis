package org.molgenis.data.mysql;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchRepositoryDecoratorCollection;
import org.molgenis.data.elasticsearch.SearchService;

public abstract class MysqlRepositoryCollection extends SearchRepositoryDecoratorCollection
{
	public static final String NAME = "MySQL";

	public MysqlRepositoryCollection(SearchService searchService, DataService dataService)
	{
		super(searchService, dataService, NAME);
	}

	@Override
	protected Repository createRepository(EntityMetaData entityMeta)
	{
		MysqlRepository repository = createMysqlRepository();
		repository.setMetaData(entityMeta);
		repository.create();

		return repository;
	}

	/**
	 * Return a spring managed prototype bean
	 */
	protected abstract MysqlRepository createMysqlRepository();

}