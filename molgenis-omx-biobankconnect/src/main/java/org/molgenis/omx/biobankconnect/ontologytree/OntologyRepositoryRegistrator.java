package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchService;
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
		// Register ontology info
		dataService.addRepository(new OntologyIndexRepository(OntologyIndexRepository.DEFAULT_ONTOLOGY_REPO,
				ontologyService, searchService));

		for (Hit hit : searchService.search(
				new SearchRequest(null, new QueryImpl().eq(OntologyRepository.ENTITY_TYPE,
						OntologyRepository.TYPE_ONTOLOGY), null)).getSearchHits())
		{
			// Register ontology content (terms) using separate repos
			Map<String, Object> columnValueMap = hit.getColumnValueMap();
			String ontologyTermEntityName = columnValueMap.containsKey(OntologyTermRepository.ONTOLOGY_LABEL) ? columnValueMap
					.get(OntologyRepository.ONTOLOGY_LABEL).toString() : OntologyTermIndexRepository.DEFAULT_ONTOLOGY_TERM_REPO;
			String ontologyUrl = columnValueMap.containsKey(OntologyTermRepository.ONTOLOGY_LABEL) ? columnValueMap
					.get(OntologyRepository.ONTOLOGY_URL).toString() : OntologyTermIndexRepository.DEFAULT_ONTOLOGY_TERM_REPO;
			dataService.addRepository(new OntologyTermIndexRepository(ontologyTermEntityName, ontologyUrl,
					searchService));
		}
	}
}