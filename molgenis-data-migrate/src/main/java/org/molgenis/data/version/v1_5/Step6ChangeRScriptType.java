package org.molgenis.data.version.v1_5;

import javax.sql.DataSource;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.ElasticSearchService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.version.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migrates the R script type from 'r' to 'R' version 1.4.3 to 1.5
 * 
 * @author mdehaan
 *
 */
@Component
public class Step6ChangeRScriptType extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step6ChangeRScriptType.class);

	@Autowired
	private DataServiceImpl dataService;

	@Autowired
	private ElasticSearchService elasticSearchService;

	public Step6ChangeRScriptType(DataSource dataSource)
	{
		super(2, 3);
	}

	@Override
	public void upgrade()
	{
		LOG.info("Changing old r script type to R");
		Entity scriptType = dataService.findOne("ScriptType", "r");
		scriptType.set("name", "R");
		dataService.update("ScriptType", scriptType);

		LOG.info("Set script type for existing scripts");
		Iterable<Entity> scriptEntities = dataService.findAll("Script");
		scriptEntities.forEach(entity -> entity.set("type", dataService.findOne("ScriptType", "R")));
		dataService.update("Script", scriptEntities);
	}
}
