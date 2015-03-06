package org.molgenis.ontology.importer;

import org.molgenis.data.importer.ImportServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class OntologyImporterServiceRegistrator implements ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(OntologyImporterServiceRegistrator.class);
	private final OntologyImportService ontologyImportService;
	private final ImportServiceFactory importServiceFactory;

	@Autowired
	public OntologyImporterServiceRegistrator(OntologyImportService ontologyImporterService,
			ImportServiceFactory importServiceFactory)
	{
		super();
		this.ontologyImportService = ontologyImporterService;
		this.importServiceFactory = importServiceFactory;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		importServiceFactory.addImportService(ontologyImportService);
		LOG.info("Registered ontology import service");
	}

}
