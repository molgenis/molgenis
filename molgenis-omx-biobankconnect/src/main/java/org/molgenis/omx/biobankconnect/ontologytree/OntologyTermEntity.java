package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.omx.biobankconnect.ontologyindexer.AsyncOntologyIndexer;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;

public class OntologyTermEntity extends AbstractOntologyEntity
{
	private static final long serialVersionUID = 1L;

	public OntologyTermEntity(Hit hit, EntityMetaData entityMetaData, SearchService searchService)
	{
		super(hit, entityMetaData, searchService);
	}

	@Override
	public Object get(String attributeName)
	{
		Map<String, Object> columnValueMap = hit.getColumnValueMap();

		if (attributeName.equalsIgnoreCase(OntologyTermQueryRepository.ID))
		{
			return hit.getId();
		}

		if (attributeName.equalsIgnoreCase(OntologyTermQueryRepository.FIELDTYPE))
		{
			return Boolean.parseBoolean(columnValueMap.get(OntologyTermIndexRepository.LAST).toString()) ? MolgenisFieldTypes.STRING
					.toString().toUpperCase() : MolgenisFieldTypes.COMPOUND.toString().toUpperCase();
		}

		if (attributeName.equalsIgnoreCase("attributes"))
		{
			List<OntologyTermEntity> refEntities = new ArrayList<OntologyTermEntity>();
			if (!Boolean.parseBoolean(columnValueMap.get(OntologyTermIndexRepository.LAST).toString()))
			{
				String currentNodePath = columnValueMap.get(OntologyTermIndexRepository.NODE_PATH).toString();
				String currentOntologyTermIri = columnValueMap.get(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI)
						.toString();
				String ontologyIri = columnValueMap.get(OntologyTermIndexRepository.ONTOLOGY_IRI).toString();
				Query q = new QueryImpl().eq(OntologyTermIndexRepository.PARENT_NODE_PATH, currentNodePath).and()
						.eq(OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_IRI, currentOntologyTermIri)
						.pageSize(Integer.MAX_VALUE);
				String documentType = AsyncOntologyIndexer.createOntologyTermDocumentType(ontologyIri);
				SearchRequest searchRequest = new SearchRequest(documentType, q, null);
				SearchResult result = searchService.search(searchRequest);
				for (Hit hit : result.getSearchHits())
				{
					refEntities.add(new OntologyTermEntity(hit, getEntityMetaData(), searchService));
				}
			}

			return refEntities;
		}

		return columnValueMap.containsKey(attributeName) ? columnValueMap.get(attributeName) : null;
	}
}
