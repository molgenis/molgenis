package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;

public class OntologyTermIndexEntity extends IndexEntity
{
	private static final long serialVersionUID = 1L;

	public OntologyTermIndexEntity(Hit hit, EntityMetaData entityMetaData, SearchService searchService)
	{
		super(hit, entityMetaData, searchService);
	}

	@Override
	public Object get(String attributeName)
	{
		Map<String, Object> columnValueMap = hit.getColumnValueMap();

		if (attributeName.equalsIgnoreCase(Characteristic.ID))
		{
			return hit.getId();
		}

		if (attributeName.equalsIgnoreCase(OntologyTermIndexRepository.FIELDTYPE))
		{
			return Boolean.parseBoolean(columnValueMap.get(OntologyTermRepository.LAST).toString()) ? MolgenisFieldTypes.STRING
					.toString().toUpperCase() : MolgenisFieldTypes.COMPOUND.toString().toUpperCase();
		}

		if (attributeName.equalsIgnoreCase("attributes"))
		{
			List<OntologyTermIndexEntity> refEntities = new ArrayList<OntologyTermIndexEntity>();
			if (!Boolean.parseBoolean(columnValueMap.get(OntologyTermRepository.LAST).toString()))
			{
				String currentNodePath = columnValueMap.get(OntologyTermRepository.NODE_PATH).toString();
				String currentOntologyTermUrl = columnValueMap.get(OntologyTermRepository.ONTOLOGY_TERM_IRI).toString();
				String ontologyUrl = columnValueMap.get(OntologyTermRepository.ONTOLOGY_IRI).toString();
				Query q = new QueryImpl().eq(OntologyTermRepository.PARENT_NODE_PATH, currentNodePath).and()
						.eq(OntologyTermRepository.PARENT_ONTOLOGY_TERM_URL, currentOntologyTermUrl).pageSize(500);
				String documentType = OntologyService.createOntologyTermDocumentType(ontologyUrl);
				SearchRequest searchRequest = new SearchRequest(documentType, q, null);
				SearchResult result = searchService.search(searchRequest);
				for (Hit hit : result.getSearchHits())
				{
					refEntities.add(new OntologyTermIndexEntity(hit, getEntityMetaData(), searchService));
				}
			}

			return refEntities;
		}

		return columnValueMap.containsKey(attributeName) ? columnValueMap.get(attributeName) : null;
	}
}
