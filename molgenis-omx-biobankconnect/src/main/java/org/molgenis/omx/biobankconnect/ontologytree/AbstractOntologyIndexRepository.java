package org.molgenis.omx.biobankconnect.ontologytree;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractOntologyIndexRepository implements Repository, Queryable
{
	protected DefaultEntityMetaData entityMetaData = null;
	protected final SearchService searchService;
	protected final Map<Integer, String> identifierMap = new HashMap<Integer, String>();
	protected final String entityName;

	@Autowired
	public AbstractOntologyIndexRepository(String entityName, SearchService searchService)
	{
		this.entityName = entityName;
		this.searchService = searchService;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return findAll(new QueryImpl()).iterator();
	}

	@Override
	public void close() throws IOException
	{
		identifierMap.clear();
	}

	public Hit findOneInternal(Query q)
	{
		for (Hit hit : searchService.search(new SearchRequest(null, q, null)).getSearchHits())
		{
			String id = hit.getId();
			int hashCode = id.hashCode();
			if (!identifierMap.containsKey(hashCode))
			{
				identifierMap.put(hashCode, id);
			}
			return hit;
		}
		return null;
	}

	public Hit findOneInternal(Integer id)
	{
		if (identifierMap.containsKey(id))
		{
			Hit hit = searchService.searchById(null, identifierMap.get(id));
			return hit;
		}
		return null;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(entityName);
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.ONTOLOGY_TERM_IRI,
					FieldTypeEnum.HYPERLINK));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.ONTOLOGY_TERM,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.SYNONYMS,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.ENTITY_TYPE,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.NODE_PATH,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.PARENT_NODE_PATH,
					FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData("fieldType", FieldTypeEnum.ENUM));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.LAST,
					FieldTypeEnum.BOOL));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.ROOT,
					FieldTypeEnum.BOOL));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(
					OntologyTermRepository.ONTOLOGY_TERM_DEFINITION, FieldTypeEnum.STRING));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermRepository.ONTOLOGY_IRI,
					FieldTypeEnum.HYPERLINK));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyRepository.ONTOLOGY_LABEL,
					FieldTypeEnum.STRING));
			DefaultAttributeMetaData childrenAttributeMetatData = new DefaultAttributeMetaData("attributes",
					FieldTypeEnum.MREF);
			childrenAttributeMetatData.setRefEntity(entityMetaData);
			entityMetaData.addAttributeMetaData(childrenAttributeMetatData);
		}
		return entityMetaData;
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long count()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOne(Integer id, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName()
	{
		return getEntityMetaData().getName();
	}
}
