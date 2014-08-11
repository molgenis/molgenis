package org.molgenis.omx.biobankconnect.ontology.repository;

import java.io.IOException;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractOntologyRepository implements Repository
{
	protected DefaultEntityMetaData entityMetaData = null;
	protected final SearchService searchService;
	protected final String entityName;
	public final static String ID = "id";
	public final static String NODE_PATH = "nodePath";
	public final static String PARENT_NODE_PATH = "parentNodePath";
	public final static String PARENT_ONTOLOGY_TERM_URL = "parentOntologyTermIRI";
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
	public final static String ONTOLOGY_LABEL = "ontologyLabel";
	public final static String ENTITY_TYPE = "entity_type";
	public final static String TYPE_ONTOLOGYTERM = "ontologyTerm";
	public final static String CHIDLREN = "children";

	@Autowired
	public AbstractOntologyRepository(String entityName, SearchService searchService)
	{
		this.entityName = entityName;
		this.searchService = searchService;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(entityName);
			DefaultAttributeMetaData attributeId = new DefaultAttributeMetaData(Characteristic.ID);
			attributeId.setIdAttribute(true);
			entityMetaData.addAttributeMetaData(attributeId);
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(
					OntologyTermIndexRepository.ONTOLOGY_TERM_IRI, FieldTypeEnum.HYPERLINK));
			DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(
					OntologyTermIndexRepository.ONTOLOGY_TERM);
			attributeMetaData.setLabelAttribute(true);
			entityMetaData.addAttributeMetaData(attributeMetaData);
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.SYNONYMS));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ENTITY_TYPE));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.NODE_PATH));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(
					OntologyTermIndexRepository.PARENT_NODE_PATH));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(
					OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_URL));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELDTYPE));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.LAST,
					FieldTypeEnum.BOOL));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ROOT,
					FieldTypeEnum.BOOL));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(
					OntologyTermIndexRepository.ONTOLOGY_TERM_DEFINITION));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ONTOLOGY_IRI,
					FieldTypeEnum.HYPERLINK));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyIndexRepository.ONTOLOGY_LABEL));
			DefaultAttributeMetaData childrenAttributeMetatData = new DefaultAttributeMetaData("attributes",
					FieldTypeEnum.MREF);
			childrenAttributeMetatData.setRefEntity(entityMetaData);
			entityMetaData.addAttributeMetaData(childrenAttributeMetatData);
			entityMetaData.setIdAttribute(Characteristic.ID);
			entityMetaData.setIdAttribute(OntologyTermIndexRepository.ONTOLOGY_TERM);
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

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
