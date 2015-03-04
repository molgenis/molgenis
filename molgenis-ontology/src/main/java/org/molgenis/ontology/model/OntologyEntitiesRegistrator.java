package org.molgenis.ontology.model;

import org.molgenis.data.DataService;
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

	@Autowired
	public OntologyEntitiesRegistrator(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (dataService.getMeta().getPackage(OntologyPackage.PACKAGE_NAME) == null)
		{
			LOG.info("Created package " + OntologyPackage.PACKAGE_NAME);
			dataService.getMeta().addPackage(OntologyPackage.getPackageInstance());
		}
		else
		{
			LOG.info("Package " + OntologyPackage.PACKAGE_NAME + " existed");
		}

		if (!dataService.hasRepository(OntologyMetaData.ENTITY_NAME))
		{
			LOG.info("Created table " + OntologyMetaData.ENTITY_NAME);
			dataService.getMeta().addEntityMeta(OntologyMetaData.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + OntologyMetaData.ENTITY_NAME + " existed");
		}

		if (!dataService.hasRepository(OntologyTermSynonymMetaData.ENTITY_NAME))
		{
			LOG.info("Created table " + OntologyTermSynonymMetaData.ENTITY_NAME);
			dataService.getMeta().addEntityMeta(OntologyTermSynonymMetaData.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + OntologyTermSynonymMetaData.ENTITY_NAME + " existed");
		}

		if (!dataService.hasRepository(OntologyTermDynamicAnnotationMetaData.ENTITY_NAME))
		{
			LOG.info("Created table " + OntologyTermDynamicAnnotationMetaData.ENTITY_NAME);
			dataService.getMeta().addEntityMeta(OntologyTermDynamicAnnotationMetaData.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + OntologyTermDynamicAnnotationMetaData.ENTITY_NAME + " existed");
		}

		if (!dataService.hasRepository(OntologyTermNodePathMetaData.ENTITY_NAME))
		{
			LOG.info("Created table " + OntologyTermNodePathMetaData.ENTITY_NAME);
			dataService.getMeta().addEntityMeta(OntologyTermNodePathMetaData.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + OntologyTermNodePathMetaData.ENTITY_NAME + " existed");
		}

		if (!dataService.hasRepository(OntologyTermMetaData.ENTITY_NAME))
		{
			LOG.info("Created table " + OntologyTermMetaData.ENTITY_NAME);
			dataService.getMeta().addEntityMeta(OntologyTermMetaData.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + OntologyTermMetaData.ENTITY_NAME + " existed");
		}
	}
}
