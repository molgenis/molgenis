package org.molgenis.omx.biobankconnect.ontologytree;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
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

	@Autowired
	public OntologyRepositoryRegistrator(SearchService searchService, DataService dataService)
	{
		this.searchService = searchService;
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
		// Register ontology info
		dataService.addRepository(new OntologyIndexRepository(searchService));

		for (Hit hit : searchService.search(
				new SearchRequest(null, new QueryImpl().eq(OntologyRepository.ENTITY_TYPE,
						OntologyRepository.TYPE_ONTOLOGY), null)).getSearchHits())
		{
			// Register ontology content (terms) using separate repos
			dataService.addRepository(new OntologyTermIndexRepository(hit, searchService));
		}
	}
}