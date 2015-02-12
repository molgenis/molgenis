package org.molgenis.ontology.repository;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractOntologyRepository implements Repository
{
	protected DefaultEntityMetaData entityMetaData = null;
	protected final SearchService searchService;
	protected final String entityName;
	public final static String ID = "id";
	public final static String NODE_PATH = "nodePath";
	public final static String PARENT_NODE_PATH = "parentNodePath";
	public final static String PARENT_ONTOLOGY_TERM_IRI = "parentOntologyTermIRI";
	public final static String FIELDTYPE = "fieldType";
	public final static String ROOT = "root";
	public final static String LAST = "isLast";
	public final static String ONTOLOGY_IRI = "ontologyIRI";
	public final static String ONTOLOGY_NAME = "ontologyName";
	public final static String ONTOLOGY_TERM = "ontologyTerm";
	public final static String ONTOLOGY_TERM_DEFINITION = "definition";
	public final static String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	public final static String SYNONYMS = "ontologyTermSynonym";
	public final static String ALTERNATIVE_DEFINITION = "alternativeDefinition";
	public final static String ENTITY_TYPE = "entity_type";
	public final static String TYPE_ONTOLOGYTERM = "ontologyTerm";
	public final static String CHIDLREN = "children";
	public final static String ILLEGAL_CHARACTERS_PATTERN = "[^(a-zA-Z0-9 )]";
	public final static String ILLEGAL_CHARACTERS_REPLACEMENT = "\\s";
	public final static String NODE_PATH_REPLACEMENT_PATTERN = "\\.[0-9]+$";
	public final static String MULTI_WHITESPACES = " +";
	public final static String SINGLE_WHITESPACE = " ";

	@Autowired
	public AbstractOntologyRepository(String entityName, SearchService searchService)
	{
		if (searchService == null) throw new IllegalArgumentException("SearchService is null!");
		if (StringUtils.isEmpty(entityName)) throw new IllegalArgumentException("The ontology entityName is null!");
		this.entityName = entityName;
		this.searchService = searchService;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(entityName);
			DefaultAttributeMetaData attributeId = new DefaultAttributeMetaData(ID);
			attributeId.setIdAttribute(true);
			entityMetaData.addAttributeMetaData(attributeId);
			entityMetaData
					.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_IRI, FieldTypeEnum.HYPERLINK));
			DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(ONTOLOGY_TERM);
			attributeMetaData.setLabelAttribute(true);
			entityMetaData.addAttributeMetaData(attributeMetaData);
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(SYNONYMS));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ENTITY_TYPE));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(NODE_PATH));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(PARENT_NODE_PATH));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(PARENT_ONTOLOGY_TERM_IRI));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELDTYPE));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(LAST, FieldTypeEnum.BOOL));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ROOT, FieldTypeEnum.BOOL));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_DEFINITION));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_NAME));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_IRI, FieldTypeEnum.HYPERLINK));
			DefaultAttributeMetaData childrenAttributeMetaData = new DefaultAttributeMetaData("attributes",
					FieldTypeEnum.MREF);
			childrenAttributeMetaData.setRefEntity(entityMetaData);
			entityMetaData.addAttributeMetaData(childrenAttributeMetaData);
		}
		return entityMetaData;
	}

	@Override
	public String getName()
	{
		return getEntityMetaData().getName();
	}

	@Override
	public void close() throws IOException
	{

	}

	public abstract <E extends Entity> Iterable<E> iterator(Class<E> clazz);

	public abstract String getUrl();
}
