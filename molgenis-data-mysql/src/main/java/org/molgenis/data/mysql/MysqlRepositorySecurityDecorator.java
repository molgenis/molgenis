package org.molgenis.data.mysql;

import javax.sql.DataSource;

import org.molgenis.data.CrudRepositorySecurityDecorator;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.validation.EntityValidator;

public class MysqlRepositorySecurityDecorator extends CrudRepositorySecurityDecorator implements
		ManageableCrudRepository
{
	private final ManageableCrudRepository manageableCrudRepository;

	public MysqlRepositorySecurityDecorator(ManageableCrudRepository decoratedRepository)
	{
		super(decoratedRepository);
		this.manageableCrudRepository = decoratedRepository;
	}

	@Override
	public void create()
	{
		manageableCrudRepository.create();
	}

	@Override
	public void drop()
	{
		manageableCrudRepository.drop();
	}

	@Override
	public void setMetaData(EntityMetaData entityMD)
	{
		manageableCrudRepository.setMetaData(entityMD);
	}

	@Override
	public void setRepositoryCollection(RepositoryCollection repositoryCollection)
	{
		manageableCrudRepository.setRepositoryCollection(repositoryCollection);
	}

	@Override
	public void setDataSource(DataSource dataSource)
	{
		manageableCrudRepository.setDataSource(dataSource);
	}

	@Override
	public void setValidator(EntityValidator validator)
	{
		manageableCrudRepository.setValidator(validator);
	}

	@Override
	public void truncate()
	{
		manageableCrudRepository.truncate();
	}

	@Override
	public void populateWithQuery(String insertQuery)
	{
		manageableCrudRepository.populateWithQuery(insertQuery);
	}

}
