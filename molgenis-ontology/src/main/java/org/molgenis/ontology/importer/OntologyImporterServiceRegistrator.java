package org.molgenis.ontology.importer;

import org.molgenis.data.importer.ImportServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class OntologyImporterServiceRegistrator implements ApplicationListener<ContextRefreshedEvent>
{
	private final OntologyImporterService ontologyImporterService;
	private final ImportServiceFactory importServiceFactory;

	@Autowired
	public OntologyImporterServiceRegistrator(OntologyImporterService ontologyImporterService,
			ImportServiceFactory importServiceFactory)
	{
		super();
		this.ontologyImporterService = ontologyImporterService;
		this.importServiceFactory = importServiceFactory;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		importServiceFactory.addImportService(ontologyImporterService);
	}

}
