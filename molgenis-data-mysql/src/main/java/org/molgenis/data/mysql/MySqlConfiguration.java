package org.molgenis.data.mysql;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.elasticsearch.ElasticSearchService;
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
	private ElasticSearchService elasticSearchService;

	@Autowired
	private DataSource dataSource;

	@Bean
	@Scope("prototype")
	public MysqlRepository mysqlRepository()
	{
		return new MysqlRepository(dataSource, new MysqlEntityValidator(dataService, new EntityAttributesValidator()));
	}

	@Bean
	public MysqlEntityMetaDataRepository entityMetaDataRepository()
	{
		return new MysqlEntityMetaDataRepository(dataSource, new MysqlEntityValidator(dataService,
				new EntityAttributesValidator()));
	}

	@Bean
	public MysqlAttributeMetaDataRepository attributeMetaDataRepository()
	{
		return new MysqlAttributeMetaDataRepository(dataSource, new MysqlEntityValidator(dataService,
				new EntityAttributesValidator()));
	}

	@Bean
	public MysqlRepositoryCollection mysqlRepositoryCollection()
	{
		return new MysqlRepositoryCollection(dataSource, dataService, entityMetaDataRepository(),
				attributeMetaDataRepository(), elasticSearchService)
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
