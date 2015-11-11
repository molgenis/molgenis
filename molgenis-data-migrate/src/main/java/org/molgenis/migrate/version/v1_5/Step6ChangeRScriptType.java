package org.molgenis.migrate.version.v1_5;

import static com.google.common.collect.Iterables.transform;

import javax.sql.DataSource;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MySqlEntityFactory;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.script.Script;
import org.molgenis.script.ScriptParameter;
import org.molgenis.script.ScriptType;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Migrates the R script type from 'r' to 'R' version 1.4.3 to 1.5
 * 
 * @author mdehaan
 *
 */
public class Step6ChangeRScriptType extends MolgenisUpgrade
{
	private static final String SWAP_SCRIPT_TYPE_NAME = "Step6ChangeRScriptType";
	private static final String OLD_SCRIPT_TYPE_NAME = "r";
	private static final String NEW_SCRIPT_TYPE_NAME = "R";

	private static final Logger LOG = LoggerFactory.getLogger(Step6ChangeRScriptType.class);

	private final SearchService searchService;
	private Repository scriptTypeRepo;
	private Repository scriptRepo;
	private DataSource dataSource;
	private MysqlRepositoryCollection mysql;
	private MetaDataService metaData;

	public Step6ChangeRScriptType(DataSource dataSource, SearchService searchService)
	{
		super(5, 6);
		this.dataSource = dataSource;
		this.searchService = searchService;
	}

	@Override
	public void upgrade()
	{
		LOG.info("Changing old r script type to R...");

		initializeUndecoratedMysqlRepository();

		Entity swapScriptType = addScriptType(SWAP_SCRIPT_TYPE_NAME);
		updateScriptTypeInScriptEntities(swapScriptType, OLD_SCRIPT_TYPE_NAME);
		scriptTypeRepo.deleteById(OLD_SCRIPT_TYPE_NAME);

		Entity newScriptType = addScriptType(NEW_SCRIPT_TYPE_NAME);
		updateScriptTypeInScriptEntities(newScriptType, SWAP_SCRIPT_TYPE_NAME);
		scriptTypeRepo.deleteById(SWAP_SCRIPT_TYPE_NAME);

		rebuildElasticSearchIndices();

		LOG.info("Updating R script type DONE");
	}

	private Iterable<Entity> updateScriptTypeInScriptEntities(Entity newScriptType, String currentScriptType)
	{
		Iterable<Entity> rScriptEntities = scriptRepo.findAll(QueryImpl.EQ(Script.TYPE, currentScriptType));
		updateScriptType(newScriptType, rScriptEntities);
		return rScriptEntities;
	}

	private void updateScriptType(Entity newScriptType, Iterable<Entity> rScriptEntities)
	{
		scriptRepo.update(transform(rScriptEntities, entity -> {
			entity.set(Script.TYPE, newScriptType);
			return entity;
		}));
	}

	private void initializeUndecoratedMysqlRepository()
	{
		DataServiceImpl dataService = new DataServiceImpl();
		EntityManager entityResolver = new EntityManagerImpl(dataService);
		MySqlEntityFactory mySqlEntityFactory = new MySqlEntityFactory(entityResolver, dataService);

		// Get the undecorated repos
		mysql = new MysqlRepositoryCollection()
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				return new MysqlRepository(dataService, mySqlEntityFactory, dataSource,
						new AsyncJdbcTemplate(new JdbcTemplate(dataSource)));
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new UnsupportedOperationException();
			}
		};
		metaData = new MetaDataServiceImpl(dataService);
		RunAsSystemProxy.runAsSystem(this::initRepositories);
	}

	private Void initRepositories()
	{
		metaData.setDefaultBackend(mysql);
		mysql.addEntityMeta(ScriptType.META_DATA);
		mysql.addEntityMeta(Script.META_DATA);
		mysql.addEntityMeta(ScriptParameter.META_DATA);
		scriptTypeRepo = metaData.addEntityMeta(ScriptType.META_DATA);
		scriptRepo = metaData.addEntityMeta(Script.META_DATA);
		metaData.addEntityMeta(ScriptParameter.META_DATA);
		return null;
	}

	private void rebuildElasticSearchIndices()
	{
		searchService.rebuildIndex(scriptTypeRepo, ScriptType.META_DATA);
		searchService.rebuildIndex(scriptRepo, Script.META_DATA);
	}

	private Entity addScriptType(String name)
	{
		Entity swap = new MapEntity(ScriptType.META_DATA);
		swap.set(ScriptType.NAME, name);
		scriptTypeRepo.add(swap);
		return swap;
	}
}
