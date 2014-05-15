package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;

public class OntologyIndexEntity extends IndexEntity
{
	private static final long serialVersionUID = 1L;
	private final OntologyService ontologyService;

	public OntologyIndexEntity(Hit hit, EntityMetaData entityMetaData, OntologyService ontologyService,
			SearchService searchService)
	{
		super(hit, entityMetaData, searchService);
		this.ontologyService = ontologyService;
	}

	@Override
	public Object get(String attributeName)
	{
		Map<String, Object> columnValueMap = hit.getColumnValueMap();

		if (attributeName.equalsIgnoreCase(Characteristic.ID))
		{
			return hit.getId();
		}

		if (attributeName.equalsIgnoreCase(OntologyIndexRepository.FIELDTYPE))
		{
			String documentType = OntologyService.createOntologyDocumentType(columnValueMap.get(
					OntologyRepository.ONTOLOGY_URL).toString());
			SearchResult result = searchService.search(new SearchRequest(documentType, new QueryImpl(), null));
			return result.getTotalHitCount() == 0 ? MolgenisFieldTypes.STRING.toString().toUpperCase() : MolgenisFieldTypes.COMPOUND
					.toString().toUpperCase();
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
			List<OntologyTermIndexEntity> refEntities = new ArrayList<OntologyTermIndexEntity>();

			for (Hit hit : ontologyService.getRootOntologyTerms(this.hit.getColumnValueMap()
					.get(OntologyTermRepository.ONTOLOGY_IRI).toString()))
			{
				refEntities.add(new OntologyTermIndexEntity(hit, getEntityMetaData(), searchService));
			}

			return refEntities;
		}

		return columnValueMap.containsKey(attributeName) ? columnValueMap.get(attributeName) : null;
	}
}
