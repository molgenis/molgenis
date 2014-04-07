package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyTermIndexRepository extends AbstractOntologyIndexRepository
{
	private final OntologyTermRepository ontologyTermRepository;
	private final static String DEFAULT_ONTOLOGY_TERM_REPO = "ontologytermindex";
	private final static String BASE_URL = "ontologytermindex://";
	private final Hit ontologyTerm;

	@Autowired
	public OntologyTermIndexRepository(Hit hit, SearchService searchService)
	{
		super(searchService);
		Map<String, Object> columnValueMap = hit.getColumnValueMap();
		String ontologyTermEntityName = columnValueMap.containsKey(OntologyTermRepository.ONTOLOGY_LABEL) ? columnValueMap
				.get(OntologyRepository.ONTOLOGY_LABEL).toString() : DEFAULT_ONTOLOGY_TERM_REPO;
		this.ontologyTermRepository = new OntologyTermRepository(null, ontologyTermEntityName);
		this.ontologyTerm = hit;
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		// Set<String> duplicatedTerms = new HashSet<String>();
		List<Entity> entities = new ArrayList<Entity>();
		if (q.getRules().size() > 0) q.and();
		q.eq(OntologyTermRepository.ENTITY_TYPE, OntologyTermRepository.TYPE_ONTOLOGYTERM);
		for (Hit hit : searchService.search(new SearchRequest(null, q, null)).getSearchHits())
		{
			// String ontologyTermUrl =
			// hit.getColumnValueMap().get(OntologyTermRepository.ONTOLOGY_TERM_IRI).toString();
			// if (!duplicatedTerms.contains(ontologyTermUrl))
			// {
			// duplicatedTerms.add(ontologyTermUrl);
			String id = hit.getId();
			int hashCode = id.hashCode();
			if (!identifierMap.containsKey(hashCode))
			{
				identifierMap.put(hashCode, id);
			}
			entities.add(new OntologyIndexEntity(hit, getEntityMetaData(), identifierMap, searchService));
			// }
		}
		return entities;
	}

	@Override
	public long count(Query q)
	{
		String documentType = "ontologyTerm-"
				+ ontologyTerm.getColumnValueMap().get(OntologyRepository.ONTOLOGY_URL).toString();
		SearchResult result = searchService.search(new SearchRequest(documentType, q, null));

		return result.getTotalHitCount();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(ontologyTermRepository.getName());
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.ROOT,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.LAST,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.ENTITY_TYPE,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.ONTOLOGY_IRI,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.ONTOLOGY_TERM,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(
					OntologyTermRepository.ONTOLOGY_TERM_DEFINITION, FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.ONTOLOGY_TERM_IRI,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.ONTOLOGY_LABEL,
					FieldTypeEnum.STRING));
			DefaultAttributeMetaData childrenAttributeMetatData = new DefaultAttributeMetaData(
					OntologyTermRepository.CHIDLREN, FieldTypeEnum.MREF);
			childrenAttributeMetatData.setRefEntity(entityMetaData);
			entityMetaData.addAttributeMetaData(childrenAttributeMetatData);
		}
		return entityMetaData;
	}

	@Override
	public String getUrl()
	{
		return BASE_URL + ontologyTermRepository.getName();
	}
}
