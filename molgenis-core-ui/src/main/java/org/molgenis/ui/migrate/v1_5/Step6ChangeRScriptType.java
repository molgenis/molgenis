package org.molgenis.ui.migrate.v1_5;

import javax.sql.DataSource;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.version.MolgenisUpgrade;
import org.molgenis.script.Script;
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
		initializeScriptRepositories();

		Entity swapScriptType = addScriptType(SWAP_SCRIPT_TYPE_NAME);
		Iterable<Entity> rScriptEntities = setSwapScriptTypeInRScripts(swapScriptType);
		
		scriptTypeRepo.deleteById(OLD_SCRIPT_TYPE_NAME);
		Entity newScriptType = addScriptType(NEW_SCRIPT_TYPE_NAME);
		
		updateScriptType(newScriptType, rScriptEntities);
		scriptTypeRepo.deleteById(SWAP_SCRIPT_TYPE_NAME);
		
		rebuildElasticSearchIndices();

		LOG.info("Updating R script type DONE");
	}

	private void initializeUndecoratedMysqlRepository()
	{
		DataServiceImpl dataService = new DataServiceImpl();
		// Get the undecorated repos
		mysql = new MysqlRepositoryCollection()
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
		RunAsSystemProxy.runAsSystem(() -> metaData.setDefaultBackend(mysql));
	}

	private void initializeScriptRepositories()
	{
		scriptTypeRepo = mysql.addEntityMeta(ScriptType.META_DATA);
		scriptRepo = mysql.addEntityMeta(Script.META_DATA);
	}

	private void rebuildElasticSearchIndices()
	{
		searchService.rebuildIndex(scriptTypeRepo, ScriptType.META_DATA);
		searchService.rebuildIndex(scriptRepo, Script.META_DATA);
	}

	private Iterable<Entity> setSwapScriptTypeInRScripts(Entity swapScriptType)
	{
		Iterable<Entity> rScriptEntities = scriptRepo.findAll(QueryImpl.EQ(ScriptType.NAME, OLD_SCRIPT_TYPE_NAME));
		updateScriptType(swapScriptType, rScriptEntities);
		return rScriptEntities;
	}

	private void updateScriptType(Entity swap, Iterable<Entity> rScriptEntities)
	{
		rScriptEntities.forEach(entity -> entity.set("type", swap));
		scriptRepo.update(rScriptEntities);
	}

	private Entity addScriptType(String name)
	{
		Entity swap = new MapEntity(ScriptType.META_DATA);
		swap.set(ScriptType.NAME, name);
		scriptTypeRepo.add(swap);
		return swap;
	}
}
