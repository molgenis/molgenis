package org.molgenis.ontology.service;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.ontology.Ontology;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.repository.OntologyQueryRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class OntologyRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataServiceImpl dataService;
	private final SearchService searchService;
	private final OntologyService ontologyService;

	@Autowired
	public OntologyRepositoryRegistrator(SearchService searchService, DataService dataService,
			OntologyService ontologyService)
	{
		this.searchService = searchService;
		this.dataService = (DataServiceImpl) dataService;// FIXME
		this.ontologyService = ontologyService;
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		// // Register ontology info
		OntologyQueryRepository ontologyRepository = new OntologyQueryRepository(
				OntologyQueryRepository.ENTITY_NAME, ontologyService, searchService, dataService);

		// TODO use dataService.getMeta().addEntityMetaData
		dataService.addRepository(ontologyRepository);

		for (Ontology ontology : ontologyService.getAllOntologies())
		{
			OntologyTermQueryRepository ontologyTermRepository = new OntologyTermQueryRepository(ontology.getLabel(),
					searchService, dataService, ontologyService);
			if (!dataService.hasRepository(ontologyTermRepository.getName()))
			{
				dataService.addRepository(ontologyTermRepository);
			}
		}
	}
}