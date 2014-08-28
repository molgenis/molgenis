package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyIndexRepository;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyQueryRepository;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.omx.biobankconnect.ontologyindexer.AsyncOntologyIndexer;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;

public class OntologyEntity extends AbstractOntologyEntity
{
	private static final long serialVersionUID = 1L;
	private final OntologyService ontologyService;

	public OntologyEntity(Hit hit, EntityMetaData entityMetaData, OntologyService ontologyService,
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

		if (attributeName.equalsIgnoreCase(OntologyQueryRepository.FIELDTYPE))
		{
			String documentType = AsyncOntologyIndexer.createOntologyDocumentType(columnValueMap.get(
					OntologyIndexRepository.ONTOLOGY_IRI).toString());
			SearchResult result = searchService.search(new SearchRequest(documentType, new QueryImpl(), null));
			return result.getTotalHitCount() == 0 ? MolgenisFieldTypes.STRING.toString().toUpperCase() : MolgenisFieldTypes.COMPOUND
					.toString().toUpperCase();
		}

		if (attributeName.equalsIgnoreCase(OntologyTermIndexRepository.LAST))
		{
			String documentType = AsyncOntologyIndexer.createOntologyDocumentType(columnValueMap.get(
					OntologyIndexRepository.ONTOLOGY_IRI).toString());
			SearchResult result = searchService.search(new SearchRequest(documentType, new QueryImpl(), null));
			return result.getTotalHitCount() == 0;
		}

		if (attributeName.equalsIgnoreCase(OntologyTermIndexRepository.ROOT))
		{
			return true;
		}

		if (attributeName.equalsIgnoreCase("attributes"))
		{
			List<OntologyTermEntity> refEntities = new ArrayList<OntologyTermEntity>();

			for (Hit hit : ontologyService.getRootOntologyTerms(this.hit.getColumnValueMap()
					.get(OntologyTermIndexRepository.ONTOLOGY_IRI).toString()))
			{
				refEntities.add(new OntologyTermEntity(hit, getEntityMetaData(), searchService));
			}

			return refEntities;
		}

		return columnValueMap.containsKey(attributeName) ? columnValueMap.get(attributeName) : null;
	}
}
