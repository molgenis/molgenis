package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.ApplicationContextProvider;

public class OntologyIndexEntity extends AbstractEntity
{
	private static final long serialVersionUID = 1L;
	private final EntityMetaData entityMetaData;
	private final Map<Integer, String> identifierMap;
	private final SearchService searchService;
	private final Hit hit;

	public OntologyIndexEntity(Hit hit, EntityMetaData entityMetaData, Map<Integer, String> identifierMap,
			SearchService searchService)
	{
		this.entityMetaData = entityMetaData;
		this.identifierMap = identifierMap;
		this.searchService = searchService;
		this.hit = hit;

		searchService = ApplicationContextProvider.getApplicationContext().getBean(SearchService.class);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		List<String> attributeNames = new ArrayList<String>();
		for (AttributeMetaData attribute : entityMetaData.getAttributes())
		{
			attributeNames.add(attribute.getName());
		}
		return attributeNames;
	}

	@Override
	public Integer getIdValue()
	{
		return hit.getId().hashCode();
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		return Arrays.asList(getEntityMetaData().getLabelAttribute().getName());
	}

	@Override
	public Object get(String attributeName)
	{
		Map<String, Object> columnValueMap = hit.getColumnValueMap();

		if (attributeName.equalsIgnoreCase(OntologyTermRepository.CHIDLREN))
		{
			List<OntologyIndexEntity> refEntities = new ArrayList<OntologyIndexEntity>();

			SearchRequest searchRequest = null;

			if (columnValueMap.get(OntologyRepository.ENTITY_TYPE).toString()
					.equalsIgnoreCase(OntologyRepository.TYPE_ONTOLOGY))
			{
				String ontologyUrl = columnValueMap.get(OntologyRepository.ONTOLOGY_URL).toString();
				String documentType = OntologyService.createOntologyTermDocumentType(ontologyUrl);
				Query q = new QueryImpl().eq(OntologyTermRepository.ROOT, true).pageSize(500);
				searchRequest = new SearchRequest(documentType, q, null);
			}
			else if (!Boolean.parseBoolean(columnValueMap.get(OntologyTermRepository.LAST).toString()))
			{
				String currentNodePath = columnValueMap.get(OntologyTermRepository.NODE_PATH).toString();
				String currentOntologyTermUrl = columnValueMap.get(OntologyTermRepository.ONTOLOGY_TERM_IRI).toString();
				String ontologyUrl = columnValueMap.get(OntologyTermRepository.ONTOLOGY_IRI).toString();
				Query q = new QueryImpl().eq(OntologyTermRepository.PARENT_NODE_PATH, currentNodePath).and()
						.eq(OntologyTermRepository.PARENT_ONTOLOGY_TERM_URL, currentOntologyTermUrl).pageSize(500);
				String documentType = OntologyService.createOntologyTermDocumentType(ontologyUrl);
				searchRequest = new SearchRequest(documentType, q, null);
			}

			if (searchRequest != null)
			{
				SearchResult result = searchService.search(searchRequest);
				for (Hit hit : result.getSearchHits())
				{
					String id = hit.getId();
					int hashCode = id.hashCode();
					if (!identifierMap.containsKey(hashCode))
					{
						identifierMap.put(hashCode, id);
					}
					refEntities.add(new OntologyIndexEntity(hit, getEntityMetaData(), identifierMap, searchService));
				}
			}

			return refEntities;
		}

		return columnValueMap.containsKey(attributeName) ? columnValueMap.get(attributeName) : null;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(Entity entity, boolean strict)
	{
		throw new UnsupportedOperationException();
	}
}
