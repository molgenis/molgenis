package org.molgenis.ontology.model;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class OntologyEntitiesRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger LOG = LoggerFactory.getLogger(OntologyEntitiesRegistrator.class);

	private final DataService dataService;
	private final WritableMetaDataService writableMetaDataService;
	// TODO : FIX ME, replace with dataService in the future
	private final MysqlRepositoryCollection mysqlRepositoryCollection;

	@Autowired
	public OntologyEntitiesRegistrator(DataService dataService, MysqlRepositoryCollection mysqlRepositoryCollection,
			WritableMetaDataService writableMetaDataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (mysqlRepositoryCollection == null) throw new IllegalArgumentException("MysqlRepositoryCollection is null");
		if (writableMetaDataService == null) throw new IllegalArgumentException("WritableMetaDataService is null");
		this.dataService = dataService;
		this.mysqlRepositoryCollection = mysqlRepositoryCollection;
		this.writableMetaDataService = writableMetaDataService;
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (writableMetaDataService.getPackage(OntologyPackage.PACKAGE_NAME) == null)
		{
			LOG.info("Created package " + OntologyPackage.PACKAGE_NAME);
			writableMetaDataService.addPackage(OntologyPackage.getPackageInstance());
		}
		else
		{
			LOG.info("Package " + OntologyPackage.PACKAGE_NAME + " existed");
		}

		if (!dataService.hasRepository(OntologyMetaData.ENTITY_NAME))
		{
			LOG.info("Created table " + OntologyMetaData.ENTITY_NAME);
			mysqlRepositoryCollection.add(OntologyMetaData.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + OntologyMetaData.ENTITY_NAME + " existed");
		}

		if (!dataService.hasRepository(OntologyTermSynonymMetaData.ENTITY_NAME))
		{
			LOG.info("Created table " + OntologyTermSynonymMetaData.ENTITY_NAME);
			mysqlRepositoryCollection.add(OntologyTermSynonymMetaData.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + OntologyTermSynonymMetaData.ENTITY_NAME + " existed");
		}

		if (!dataService.hasRepository(OntologyTermDynamicAnnotationMetaData.ENTITY_NAME))
		{
			LOG.info("Created table " + OntologyTermDynamicAnnotationMetaData.ENTITY_NAME);
			mysqlRepositoryCollection.add(OntologyTermDynamicAnnotationMetaData.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + OntologyTermDynamicAnnotationMetaData.ENTITY_NAME + " existed");
		}

		if (!dataService.hasRepository(OntologyTermNodePathMetaData.ENTITY_NAME))
		{
			LOG.info("Created table " + OntologyTermNodePathMetaData.ENTITY_NAME);
			mysqlRepositoryCollection.add(OntologyTermNodePathMetaData.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + OntologyTermNodePathMetaData.ENTITY_NAME + " existed");
		}

		if (!dataService.hasRepository(OntologyTermMetaData.ENTITY_NAME))
		{
			LOG.info("Created table " + OntologyTermMetaData.ENTITY_NAME);
			mysqlRepositoryCollection.add(OntologyTermMetaData.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + OntologyTermMetaData.ENTITY_NAME + " existed");
		}
	}
}
