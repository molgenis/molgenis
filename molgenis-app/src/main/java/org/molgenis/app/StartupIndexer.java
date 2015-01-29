package org.molgenis.app;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.jpa.JpaRepositoryCollection;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Reindexes all meta, mysql and jpa entities when the index is missing, at startup.
 */
@Service
public class StartupIndexer implements Ordered
{
	private static final Logger LOG = LoggerFactory.getLogger(StartupIndexer.class);
	private final SearchService searchService;
	private final DataSource dataSource;

	@Autowired
	public StartupIndexer(DataSource dataSource, SearchService searchService,
			JpaRepositoryCollection jpaRepositoryCollection) throws SQLException
	{
		this.searchService = searchService;
		this.dataSource = dataSource;

		if (indexExists())
		{
			LOG.info("Index detected, no need to reindex.");
		}
		else
		{
			LOG.info("Missing index, reindexing... ");
			indexMysqlRepos();
			indexJpaRepos(jpaRepositoryCollection);
		}
	}

	private boolean indexExists()
	{
		return searchService.hasMapping(new EntityMetaDataMetaData());
	}

	private void indexJpaRepos(JpaRepositoryCollection jpaRepositoryCollection)
	{
		jpaRepositoryCollection.getEntityNames().forEach(
				name -> index((CrudRepository) jpaRepositoryCollection.getRepositoryByEntityName(name)));
	}

	private void indexMysqlRepos()
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
		mysqlRepositoryCollection.forEach(this::index);
	}

	private void index(CrudRepository repo)
	{
		searchService.rebuildIndex(repo, repo.getEntityMetaData());
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
