package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;

public class OntologyTermIndexEntity extends IndexEntity
{
	private static final long serialVersionUID = 1L;

	public OntologyTermIndexEntity(Hit hit, EntityMetaData entityMetaData, Map<Integer, String> identifierMap,
			SearchService searchService)
	{
		super(hit, entityMetaData, identifierMap, searchService);
	}

	@Override
	public Object get(String attributeName)
	{
		Map<String, Object> columnValueMap = hit.getColumnValueMap();

		if (attributeName.equalsIgnoreCase("name"))
		{
			return columnValueMap.get(OntologyTermRepository.ONTOLOGY_TERM_IRI);
		}

		if (attributeName.equalsIgnoreCase("fieldType"))
		{
			return Boolean.parseBoolean(columnValueMap.get(OntologyTermRepository.LAST).toString()) ? FieldTypeEnum.STRING : FieldTypeEnum.COMPOUND;
		}

		if (attributeName.equalsIgnoreCase("label"))
		{
			return columnValueMap.get(OntologyTermRepository.ONTOLOGY_TERM);
		}

		if (attributeName.equalsIgnoreCase("description"))
		{
			return columnValueMap.get(OntologyTermRepository.ONTOLOGY_TERM_DEFINITION);
		}

		if (attributeName.equalsIgnoreCase("ontologyUrl"))
		{
			return columnValueMap.get(OntologyTermRepository.ONTOLOGY_IRI);
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
					String id = hit.getId();
					int hashCode = id.hashCode();
					if (!identifierMap.containsKey(hashCode))
					{
						identifierMap.put(hashCode, id);
					}
					refEntities
							.add(new OntologyTermIndexEntity(hit, getEntityMetaData(), identifierMap, searchService));
				}
			}

			return refEntities;
		}

		return columnValueMap.containsKey(attributeName) ? columnValueMap.get(attributeName) : null;
	}
}
