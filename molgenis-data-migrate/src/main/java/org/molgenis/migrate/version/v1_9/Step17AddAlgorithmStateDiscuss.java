package org.molgenis.migrate.version.v1_9;

import javax.sql.DataSource;

import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Preconditions;

/**
 * Migration for the AttributeMapping table
 * <ol>
 * <li>Add a column `algorithmState` enum('CURATED','GENERATED_HIGH','GENERATED_LOW','DISCUSS') to AttributeMapping</li>
 * </ol>
 * 
 */
public class Step17AddAlgorithmStateDiscuss extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step17AddAlgorithmStateDiscuss.class);
	private final JdbcTemplate jdbcTemplate;
	private SearchService searchService;
	private DataSource dataSource;

	public Step17AddAlgorithmStateDiscuss(DataSource dataSource, SearchService searchService)
	{
		super(16, 17);
		this.searchService = searchService;
		this.searchService = searchService;
		this.jdbcTemplate = new JdbcTemplate(Preconditions.checkNotNull(dataSource));
	}

	@Override
	public void upgrade()
	{
		LOG.info("Update metadata of the AttributeMapping from version 1.8 to 1.9");
		LOG.info("Alter AttributeMapping --- add column `algorithmState` enum('CURATED','GENERATED_HIGH','GENERATED_LOW','DISCUSS');");
		jdbcTemplate
				.execute("ALTER TABLE `AttributeMapping` ADD `algorithmState` enum('CURATED','GENERATED_HIGH','GENERATED_LOW','DISCUSS');");

		DataServiceImpl dataService = new DataServiceImpl();
		MysqlRepositoryCollection undecoratedMySQL = new MysqlRepositoryCollection()
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				return new MysqlRepository(dataService, dataSource, new AsyncJdbcTemplate(new JdbcTemplate(dataSource)));
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new UnsupportedOperationException();
			}
		};

		MetaDataService metaData = new MetaDataServiceImpl(dataService);
		RunAsSystemProxy.runAsSystem(() -> metaData.setDefaultBackend(undecoratedMySQL));

		searchService.delete(AttributeMappingMetaData.ENTITY_NAME);
		searchService.createMappings(new AttributeMappingMetaData());

		searchService.rebuildIndex(undecoratedMySQL.getRepository(AttributeMappingMetaData.ENTITY_NAME),
				new AttributeMappingMetaData());
	}
}