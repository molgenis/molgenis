package org.molgenis.data.version.v1_5;

import static org.molgenis.data.support.QueryImpl.EQ;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.meta.migrate.v1_4.AttributeMetaDataMetaData1_4;
import org.molgenis.data.meta.migrate.v1_4.EntityMetaDataMetaData1_4;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.version.MetaDataUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileCopyUtils;

import com.google.common.collect.Lists;

public class Step1 extends MetaDataUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step1.class);
	private SearchService searchService;
	private JdbcTemplate jdbcTemplate;
	private DataSource dataSource;

	// private ElasticSearchServiceF

	public Step1(SearchService searchService, DataSource dataSource)
	{
		super(0, 1);
		this.searchService = searchService;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void upgrade()
	{
		updateMetaDataDatabaseTables();
		migrateMetaData();
	}

	private void updateMetaDataDatabaseTables()
	{
		// TODO: Update metadata database tables here! (or run SQL script manually)

		InputStream in = getClass().getResourceAsStream("/2582.sql");
		try
		{
			String script = FileCopyUtils.copyToString(new InputStreamReader(in));
			String[] statements = script.split(";");
			Arrays.stream(statements).forEach(jdbcTemplate::execute);
		}
		catch (DataAccessException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void migrateMetaData()
	{
		// Create local dataservice and metadataservice
		DataServiceImpl v15MySQLDataService = new DataServiceImpl();
		MetaDataService meta = new MetaDataServiceImpl(v15MySQLDataService);

		MysqlRepositoryCollection v15MySQLBackend = new MysqlRepositoryCollection()
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				return new MysqlRepository(v15MySQLDataService, dataSource, new AsyncJdbcTemplate(new JdbcTemplate(
						dataSource)));
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new NotImplementedException("Not implemented yet");
			}
		};
		meta.setDefaultBackend(v15MySQLBackend);

		updateAttributeOrderInMysql(v15MySQLDataService, searchService);
		recreateElasticSearchMetaDataIndices(searchService);
	}

	private void updateAttributeOrderInMysql(DataServiceImpl v15MySQLDataService, SearchService v14ElasticSearchService)
	{
		LOG.info("updateAttributeOrder and set backend...");

		// save all entity metadata with attributes in proper order
		for (Entity v15EntityMetaDataEntity : v15MySQLDataService.getRepository(EntityMetaDataMetaData.ENTITY_NAME))
		{
			LOG.info("V1.5 Entity: " + v15EntityMetaDataEntity.get(EntityMetaDataMetaData1_4.SIMPLE_NAME));
			List<Entity> attributes = Lists.newArrayList(v14ElasticSearchService.search(
					EQ(AttributeMetaDataMetaData1_4.ENTITY,
							v15EntityMetaDataEntity.getString(EntityMetaDataMetaData1_4.SIMPLE_NAME)),
					new AttributeMetaDataMetaData1_4()));
			v15EntityMetaDataEntity.set(EntityMetaDataMetaData.ATTRIBUTES, attributes);
			v15EntityMetaDataEntity.set(EntityMetaDataMetaData.BACKEND, "MySQL");
			v15MySQLDataService.update(EntityMetaDataMetaData.ENTITY_NAME, v15EntityMetaDataEntity);
		}
		LOG.info("updateAttributeOrder done.");
	}

	private void recreateElasticSearchMetaDataIndices(SearchService v14ElasticSearchService)
	{
		v14ElasticSearchService.delete("entities");
		v14ElasticSearchService.delete("attributes");
		v14ElasticSearchService.delete("tags");
		v14ElasticSearchService.delete("packages");

		searchService.refresh();

		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try
		{
			searchService.createMappings(new TagMetaData());
			searchService.createMappings(new PackageMetaData());
			searchService.createMappings(new AttributeMetaDataMetaData());
			searchService.createMappings(new EntityMetaDataMetaData());
		}
		catch (IOException e)
		{
			LOG.error("error creating metadata mappings", e);
		}

		// TODO: refill from mysql!
	}

}
