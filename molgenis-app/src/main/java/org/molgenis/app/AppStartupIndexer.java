package org.molgenis.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.jpa.JpaRepositoryCollection;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.ui.StartupIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Reindexes all meta, mysql and jpa entities when the index is missing, at startup.
 */
@Service
public class AppStartupIndexer extends StartupIndexer
{
	private final DataSource dataSource;
	private final JpaRepositoryCollection jpaRepositoryCollection;

	@Autowired
	public AppStartupIndexer(DataSource dataSource, SearchService searchService,
			JpaRepositoryCollection jpaRepositoryCollection) throws SQLException
	{
		super(searchService);
		this.dataSource = dataSource;
		this.jpaRepositoryCollection = jpaRepositoryCollection;
	}

	@Override
	protected Iterable<CrudRepository> getCrudRepositories()
	{
		List<CrudRepository> repos = new ArrayList<>();

		// JPA
		jpaRepositoryCollection.getEntityNames().forEach(
				name -> repos.add(((CrudRepository) jpaRepositoryCollection.getRepositoryByEntityName(name))));

		// MYSQL
		createMysqlRepositoryCollection().forEach(repos::add);

		return repos;
	}

	private MysqlRepositoryCollection createMysqlRepositoryCollection()
	{
		MetaDataServiceImpl metaDataService = new MetaDataServiceImpl();
		MysqlRepositoryCollection mysqlRepositoryCollection = new MysqlRepositoryCollection(dataSource,
				new DataServiceImpl(), metaDataService)
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				MysqlRepository mysqlRepository = new MysqlRepository(dataSource, new AsyncJdbcTemplate(
						new JdbcTemplate(dataSource)));
				mysqlRepository.setRepositoryCollection(this);

				return mysqlRepository;
			}
		};
		metaDataService.setManageableCrudRepositoryCollection(mysqlRepositoryCollection);
		mysqlRepositoryCollection.refreshRepositories();

		return mysqlRepositoryCollection;
	}

}
