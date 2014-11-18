package org.molgenis.data.mysql;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.validation.EntityAttributesValidator;
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

	@Bean
	@Scope("prototype")
	public MysqlRepository mysqlRepository()
	{
		return new MysqlRepository(dataSource, new MysqlEntityValidator(dataService, new EntityAttributesValidator()));
	}

	@Bean
	public EntityMetaDataRepository entityMetaDataRepository()
	{
		return new EntityMetaDataRepository(dataSource, new MysqlEntityValidator(dataService,
				new EntityAttributesValidator()));
	}

	@Bean
	public AttributeMetaDataRepository attributeMetaDataRepository()
	{
		return new AttributeMetaDataRepository(dataSource, new MysqlEntityValidator(dataService,
				new EntityAttributesValidator()));
	}

	@Bean
	public MysqlRepositoryCollection mysqlRepositoryCollection()
	{
		return new MysqlRepositoryCollection(dataSource, dataService, entityMetaDataRepository(),
				attributeMetaDataRepository())
		{
			@Override
			protected MysqlRepository createMysqlRepsitory()
			{
				MysqlRepository repo = mysqlRepository();
				repo.setRepositoryCollection(this);
				return repo;
			}
		};
	}
}
