package org.molgenis.data.version.v1_6;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.version.MetaDataUpgrade;
import org.molgenis.security.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step1UpgrateMetaDataTo1_6 extends MetaDataUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step1UpgrateMetaDataTo1_6.class);
	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;
	private final SearchService searchService;

	public Step1UpgrateMetaDataTo1_6(DataSource dataSource, SearchService searchService)
	{
		super(4, 5);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSource = dataSource;
		this.searchService = searchService;
	}

	@Override
	public void upgrade()
	{
		LOG.info("Updating metadata from version 4 to 5");
		List<String> sqls = Arrays.asList("ALTER TABLE attributes ADD COLUMN visibleExpression TEXT",
				"ALTER TABLE attributes ADD COLUMN validationExpression TEXT");

		sqls.forEach(sql -> {
			try
			{
				LOG.info(sql);
				jdbcTemplate.execute(sql);
			}
			catch (DataAccessException e)
			{
				LOG.error(e.getMessage());
			}
		});
		LOG.info("Create bare mysql repository collection for the metadata...");

		DataServiceImpl dataService = new DataServiceImpl();

		// Get the undecorated repos
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
				throw new NotImplementedException("Not implemented yet");
			}
		};
		MetaDataService metaData = new MetaDataServiceImpl(dataService);
		RunAsSystemProxy.runAsSystem(() -> metaData.setDefaultBackend(undecoratedMySQL));

		searchService.delete(AttributeMetaDataMetaData.ENTITY_NAME);
		try
		{
			searchService.createMappings(new AttributeMetaDataMetaData());
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}

		searchService.rebuildIndex(undecoratedMySQL.getRepository(AttributeMetaDataMetaData.ENTITY_NAME),
				new AttributeMetaDataMetaData());

	}

}
