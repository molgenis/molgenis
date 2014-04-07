package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.ArrayList;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;

public class OntologyIndexEntity extends IndexEntity
{
	private static final long serialVersionUID = 1L;

	public OntologyIndexEntity(Hit hit, EntityMetaData entityMetaData, Map<Integer, String> identifierMap,
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
			return columnValueMap.get(OntologyRepository.ONTOLOGY_URL);
		}

		if (attributeName.equalsIgnoreCase("fieldType"))
		{
			String documentType = OntologyService.createOntologyDocumentType(columnValueMap.get(
					OntologyRepository.ONTOLOGY_URL).toString());
			SearchResult result = searchService.search(new SearchRequest(documentType, new QueryImpl(), null));
			return result.getTotalHitCount() == 0 ? FieldTypeEnum.STRING : FieldTypeEnum.COMPOUND;
		}

		if (attributeName.equalsIgnoreCase("label"))
		{
			return columnValueMap.get(OntologyRepository.ONTOLOGY_LABEL);
		}

		if (attributeName.equalsIgnoreCase("description"))
		{
			return null;
		}

		if (attributeName.equalsIgnoreCase("ontologyUrl"))
		{
			return columnValueMap.get(OntologyRepository.ONTOLOGY_URL);
		}

		if (attributeName.equalsIgnoreCase(OntologyTermRepository.LAST))
		{
			String documentType = OntologyService.createOntologyDocumentType(columnValueMap.get(
					OntologyRepository.ONTOLOGY_URL).toString());
			SearchResult result = searchService.search(new SearchRequest(documentType, new QueryImpl(), null));
			return result.getTotalHitCount() == 0;
		}

		if (attributeName.equalsIgnoreCase(OntologyTermRepository.ROOT))
		{
			return true;
		}

		if (attributeName.equalsIgnoreCase("attributes"))
		{
			return new ArrayList<OntologyTermIndexEntity>();
		}

		return columnValueMap.containsKey(attributeName) ? columnValueMap.get(attributeName) : null;
	}
}
