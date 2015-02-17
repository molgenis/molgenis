package org.molgenis.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.jpa.JpaRepositoryCollection;
import org.molgenis.ui.StartupIndexer;
import org.springframework.beans.factory.annotation.Autowired;
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
	protected Iterable<Repository> getRepositories()
	{
		List<Repository> repos = new ArrayList<>();

		// JPA
		jpaRepositoryCollection.getEntityNames()
				.forEach(name -> repos.add(jpaRepositoryCollection.getRepository(name)));

		// MYSQL
		// createMysqlRepositoryCollection().forEach(repos::add);

		return repos;
	}

	// TODO
	// private MysqlRepositoryCollection createMysqlRepositoryCollection()
	// {
	// DataServiceImpl dataService = new DataServiceImpl(new NonDecoratingRepositoryDecoratorFactory());
	// MetaDataServiceImpl metaDataService = new MetaDataServiceImpl(dataService);
	// dataService.setMetaDataService(metaDataService);
	//
	// MysqlRepositoryCollection mysqlRepositoryCollection = new MysqlRepositoryCollection(dataSource,
	// new DataServiceImpl(), metaDataService)
	// {
	// @Override
	// protected MysqlRepository createMysqlRepository()
	// {
	// MysqlRepository mysqlRepository = new MysqlRepository(dataSource, new AsyncJdbcTemplate(
	// new JdbcTemplate(dataSource)));
	// mysqlRepository.setRepositoryCollection(this);
	//
	// return mysqlRepository;
	// }
	// };
	// metaDataService.setManageableCrudRepositoryCollection(mysqlRepositoryCollection);
	// mysqlRepositoryCollection.refreshRepositories();
	//
	// return mysqlRepositoryCollection;
	// }

}
