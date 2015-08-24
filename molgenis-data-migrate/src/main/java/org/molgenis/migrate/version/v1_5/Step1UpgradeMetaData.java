package org.molgenis.migrate.version.v1_5;

import static org.molgenis.data.support.QueryImpl.EQ;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.migrate.version.v1_4.AttributeMetaDataMetaData1_4;
import org.molgenis.migrate.version.v1_4.EntityMetaDataMetaData1_4;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileCopyUtils;

import com.google.common.collect.Lists;

/**
 * Upgrades the metadata repositories in MySQL.
 * 
 * Fills the attribute order based on the current values in ElasticSearch. It looks like ElasticSearch *does* return the
 * attribute documents in the order in which they were inserted.
 * 
 * Subsequently drops the index and recreates it.
 */
public class Step1UpgradeMetaData extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step1UpgradeMetaData.class);
	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;
	private MysqlRepositoryCollection undecoratedMySQL;
	private final SearchService searchService;

	public Step1UpgradeMetaData(DataSource dataSource, SearchService searchService)
	{
		super(0, 1);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSource = dataSource;
		this.searchService = searchService;
	}

	@Override
	public void upgrade()
	{
		LOG.info("Upgrade MySQL metadata tables...");

		LOG.info("Update metadata table structure...");
		updateMetaDataDatabaseTables();

		LOG.info("Create bare mysql repository collection for the metadata...");
		DataServiceImpl dataService = new DataServiceImpl();
		// Get the undecorated repos
		undecoratedMySQL = new MysqlRepositoryCollection()
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

		LOG.info("Read attribute order from ElasticSearch and write to the bare mysql repositories...");
		updateAttributesInMysql();

		LOG.info("Reindex metadata repositories...");
		recreateElasticSearchMetaDataIndices();
		LOG.info("Upgrade MySQL metadata tables DONE.");
	}

	private void updateMetaDataDatabaseTables()
	{
		InputStream in = getClass().getResourceAsStream("/2582.sql");
		String script;
		try
		{
			script = FileCopyUtils.copyToString(new InputStreamReader(in, "UTF-8"));
		}
		catch (IOException e)
		{
			LOG.error("Failed to read upgrade script", e);
			throw new RuntimeException(e);
		}
		for (String statement : script.split(";"))
		{
			String trimmed = statement.trim();
			try
			{
				LOG.info(trimmed);
				jdbcTemplate.execute(trimmed);
			}
			catch (DataAccessException e)
			{
				LOG.error(e.getMessage());
			}

		}
	}

	private void updateAttributesInMysql()
	{
		LOG.info("Update attribute order in MySQL...");
		Repository entityRepository = undecoratedMySQL.getRepository(EntityMetaDataMetaData.ENTITY_NAME);
		Repository attributeRepository = undecoratedMySQL.getRepository(AttributeMetaDataMetaData.ENTITY_NAME);

		// save all entity metadata with attributes in proper order
		for (Entity v15EntityMetaDataEntity : entityRepository)
		{
			LOG.info("Setting attribute order for entity: "
					+ v15EntityMetaDataEntity.get(EntityMetaDataMetaData1_4.FULL_NAME));
			List<Entity> attributes = Lists.newArrayList(searchService.search(
					EQ(AttributeMetaDataMetaData1_4.ENTITY,
							v15EntityMetaDataEntity.getString(EntityMetaDataMetaData.FULL_NAME)),
					new AttributeMetaDataMetaData1_4()));
			attributes = attributes.stream().filter(a -> a.get(AttributeMetaDataMetaData1_4.PART_OF_ATTRIBUTE) == null)
					.collect(Collectors.toList());
			for (Entity attribute : attributes)
			{
				updateAttribute(attributeRepository, attribute);
			}

			v15EntityMetaDataEntity.set(EntityMetaDataMetaData.ATTRIBUTES, attributes);
			v15EntityMetaDataEntity.set(EntityMetaDataMetaData.BACKEND, "MySQL");
			entityRepository.update(v15EntityMetaDataEntity);
		}
		LOG.info("Update attribute order done.");
	}

	/**
	 * Update an 1.4 attribute's parts attribute in the 1.5 attribute repository
	 * 
	 * @param attributeRepository
	 *            undecorated 1.5 MySQL attribute repository
	 * @param attribute_v1_4
	 *            elasticsearch 1.4 attribute document entity
	 */
	private void updateAttribute(Repository attributeRepository, Entity attribute_v1_4)
	{
		LOG.info("Setting attribute : " + attribute_v1_4.get(AttributeMetaDataMetaData1_4.NAME));
		List<Entity> attributeParts_v1_4 = Lists.newArrayList(searchService.search(
				EQ(AttributeMetaDataMetaData1_4.PART_OF_ATTRIBUTE,
						attribute_v1_4.get(AttributeMetaDataMetaData1_4.NAME)).and().eq(
						AttributeMetaDataMetaData1_4.ENTITY, attribute_v1_4.get(AttributeMetaDataMetaData1_4.ENTITY)),
				new AttributeMetaDataMetaData1_4()));
		Entity attribute_v1_5FromRepo = attributeRepository.findOne(attribute_v1_4.getIdValue());
		attribute_v1_5FromRepo.set(AttributeMetaDataMetaData.PARTS, attributeParts_v1_4);
		attributeRepository.update(attribute_v1_5FromRepo);
		for (Entity part : attributeParts_v1_4)
		{
			updateAttribute(attributeRepository, part);
		}
	}

	private void recreateElasticSearchMetaDataIndices()
	{
		LOG.info("Deleting metadata indices...");
		searchService.delete(EntityMetaDataMetaData.ENTITY_NAME);
		searchService.delete(AttributeMetaDataMetaData.ENTITY_NAME);
		searchService.delete(TagMetaData.ENTITY_NAME);
		searchService.delete(PackageMetaData.ENTITY_NAME);

		searchService.refresh();

		LOG.info("Deleting metadata indices DONE.");

		try
		{
			// sleep just a bit to be sure changes have been persisted
			Thread.sleep(1500);
		}
		catch (InterruptedException e1)
		{
		}

		LOG.info("Adding metadata indices...");

		searchService.createMappings(TagMetaData.INSTANCE);
		searchService.createMappings(PackageMetaData.INSTANCE);
		searchService.createMappings(AttributeMetaDataMetaData.INSTANCE);
		searchService.createMappings(EntityMetaDataMetaData.INSTANCE);

		LOG.info("Reindexing MySQL repositories...");

		searchService.rebuildIndex(undecoratedMySQL.getRepository(TagMetaData.ENTITY_NAME), TagMetaData.INSTANCE);
		searchService.rebuildIndex(undecoratedMySQL.getRepository(PackageMetaData.ENTITY_NAME),
				PackageMetaData.INSTANCE);
		searchService.rebuildIndex(undecoratedMySQL.getRepository(AttributeMetaDataMetaData.ENTITY_NAME),
				AttributeMetaDataMetaData.INSTANCE);
		searchService.rebuildIndex(undecoratedMySQL.getRepository(EntityMetaDataMetaData.ENTITY_NAME),
				EntityMetaDataMetaData.INSTANCE);

		LOG.info("Reindexing MySQL repositories DONE.");

	}

}
