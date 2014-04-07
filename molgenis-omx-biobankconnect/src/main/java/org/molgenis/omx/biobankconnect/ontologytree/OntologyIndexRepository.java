package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Iterables;

public class OntologyIndexRepository extends AbstractOntologyIndexRepository
{
	private final static String BASE_URL = "ontologyindex://";
	private final static String DEFAULT_ONTOLOGY_REPO = "ontologyindex";
	private final OntologyRepository ontologyRepository;

	@Autowired
	public OntologyIndexRepository(SearchService searchService)
	{
		super(searchService);
		this.ontologyRepository = new OntologyRepository(null, DEFAULT_ONTOLOGY_REPO);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		List<Entity> entities = new ArrayList<Entity>();
		q.eq(OntologyRepository.ENTITY_TYPE, OntologyRepository.TYPE_ONTOLOGY);
		for (Hit hit : searchService.search(new SearchRequest(null, q, null)).getSearchHits())
		{
			String id = hit.getId();
			int hashCode = id.hashCode();
			if (!identifierMap.containsKey(hashCode))
			{
				identifierMap.put(hashCode, id);
			}
			entities.add(new OntologyIndexEntity(hit, getEntityMetaData(), identifierMap, searchService));
		}
		return entities;
	}

	@Override
	public long count(Query q)
	{
		if (q.getRules().size() == 0)
		{
			SearchResult result = searchService.search(new SearchRequest(null, new QueryImpl().eq(
					OntologyRepository.ENTITY_TYPE, OntologyRepository.TYPE_ONTOLOGY), null));

			return result.getTotalHitCount();
		}
		return Iterables.size(findAll(new QueryImpl()));
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(ontologyRepository.getName());
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyRepository.ONTOLOGY_LABEL,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyRepository.ONTOLOGY_URL,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyRepository.ENTITY_TYPE,
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
		return BASE_URL + ontologyRepository.getName();
	}
}