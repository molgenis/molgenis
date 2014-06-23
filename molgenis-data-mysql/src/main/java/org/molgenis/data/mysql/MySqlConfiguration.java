package org.molgenis.data.mysql;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.validation.EntityValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MySqlConfiguration
{
	@Autowired
	private DataService dataService;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private EntityValidator entityValidator;

	@Bean
	@Scope("prototype")
	public ManageableCrudRepository mysqlRepository()
	{
		ManageableCrudRepository repo = new MysqlRepository();
		repo.setDataSource(dataSource);
		repo.setValidator(entityValidator);

		return repo;
	}

	@Bean
	public MysqlRepositoryCollection mysqlRepositoryCollection()
	{
		return new MysqlRepositoryCollection(dataSource, dataService)
		{
			@Override
			protected ManageableCrudRepository createMysqlRepsitory()
			{
				ManageableCrudRepository repo = mysqlRepository();
				repo.setRepositoryCollection(this);

				return repo;
			}

		};
	}
}
