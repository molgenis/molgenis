package org.molgenis.omx.biobankconnect.ontologytree;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class OntologyRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;
	private final SearchService searchService;
	private final OntologyService ontologyService;

	@Autowired
	public OntologyRepositoryRegistrator(SearchService searchService, DataService dataService,
			OntologyService ontologyService)
	{
		this.searchService = searchService;
		this.dataService = dataService;
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
		// dataService.addRepository(new OntologyQueryRepository(OntologyQueryRepository.DEFAULT_ONTOLOGY_REPO,
		// ontologyService, searchService));
		//
		// for (Hit hit : searchService.search(
		// new SearchRequest(null, new QueryImpl().eq(OntologyIndexRepository.ENTITY_TYPE,
		// OntologyIndexRepository.TYPE_ONTOLOGY), null)).getSearchHits())
		// {
		// // Register ontology content (terms) using separate repos
		// Map<String, Object> columnValueMap = hit.getColumnValueMap();
		// String ontologyTermEntityName = columnValueMap.containsKey(OntologyTermIndexRepository.ONTOLOGY_NAME) ?
		// columnValueMap
		// .get(OntologyIndexRepository.ONTOLOGY_NAME).toString() :
		// OntologyTermQueryRepository.DEFAULT_ONTOLOGY_TERM_REPO;
		// String ontologyIri = columnValueMap.containsKey(OntologyTermIndexRepository.ONTOLOGY_NAME) ? columnValueMap
		// .get(OntologyIndexRepository.ONTOLOGY_IRI).toString() :
		// OntologyTermQueryRepository.DEFAULT_ONTOLOGY_TERM_REPO;
		// dataService.addRepository(new OntologyTermQueryRepository(ontologyTermEntityName, ontologyIri,
		// searchService));
		// }
	}
}