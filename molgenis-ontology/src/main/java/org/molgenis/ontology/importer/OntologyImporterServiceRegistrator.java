package org.molgenis.ontology.importer;

import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.ontology.initializer.OntologyScriptInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class OntologyImporterServiceRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{

	private static final Logger LOG = LoggerFactory.getLogger(OntologyImporterServiceRegistrator.class);
	private final OntologyImportService ontologyImportService;
	private final ImportServiceFactory importServiceFactory;
	private final OntologyScriptInitializer ontologyScriptInitializer;

	@Autowired
	public OntologyImporterServiceRegistrator(OntologyImportService ontologyImporterService,
			ImportServiceFactory importServiceFactory, OntologyScriptInitializer ontologyScriptInitializer)
	{
		super();
		this.ontologyImportService = ontologyImporterService;
		this.importServiceFactory = importServiceFactory;
		this.ontologyScriptInitializer = ontologyScriptInitializer;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		importServiceFactory.addImportService(ontologyImportService);
		LOG.info("Registered ontology import service");
		ontologyScriptInitializer.initialize();
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}
}
